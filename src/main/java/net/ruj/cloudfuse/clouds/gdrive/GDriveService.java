package net.ruj.cloudfuse.clouds.gdrive;

import net.ruj.cloudfuse.clouds.CloudStorageService;
import net.ruj.cloudfuse.clouds.exceptions.MakeDirectoryException;
import net.ruj.cloudfuse.clouds.exceptions.MakeRootException;
import net.ruj.cloudfuse.clouds.exceptions.UploadFileException;
import net.ruj.cloudfuse.clouds.gdrive.models.File;
import net.ruj.cloudfuse.clouds.gdrive.models.FileList;
import net.ruj.cloudfuse.fuse.FuseConfiguration;
import net.ruj.cloudfuse.fuse.filesystem.CloudDirectory;
import net.ruj.cloudfuse.fuse.filesystem.CloudFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class GDriveService implements CloudStorageService {
    private static final Logger logger = LoggerFactory.getLogger(GDriveService.class);
    private final RestTemplate restTemplate;
    private String token;

    public GDriveService(String token) {
        this.token = token;
        this.restTemplate = new RestTemplate();
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        restTemplate.setRequestFactory(requestFactory);
    }

    //TODO: Handle parents and byte streams
    @Override
    public synchronized void uploadFile(CloudDirectory parent, CloudFile file) throws UploadFileException {
        logger.info("Uploading file '" + file.getPath() + "'...");
        try {
            File remoteFile = restTemplate.postForObject(
                    UriComponentsBuilder.fromUri(
                            new URI("https", "www.googleapis.com", "/drive/v3/files", "", "")
                    )
                            .queryParam("uploadType", "media")
                            .toUriString(),
                    generateFileCreateRequestEntity(file.getContents()),
                    File.class
            );
            remoteFile = restTemplate.patchForObject(
                    UriComponentsBuilder.fromUri(
                            new URI("https", "www.googleapis.com", "/drive/v3/files" + remoteFile.getId(), "", "")
                    )
                            .toUriString(),
                    generateFileMetadataRequestEntity(remoteFile, file.getPath().getFileName().toString()),
                    File.class
            );
            logger.info("Upload of file '" + remoteFile.getName() + "' completed.");
        } catch (URISyntaxException e) {
            e.printStackTrace();
            throw new UploadFileException(e);
        }
    }

    //TODO: Handle parents
    @Override
    public void makeDirectory(CloudDirectory parent, CloudDirectory directory) throws MakeDirectoryException {
        logger.info("Making folder '" + directory.getPath() + "'...");
        try {
            File remoteFolder = gDriveCreateDirectory(parent, directory.getPath().getFileName().toString());
            logger.info("Folder '" + remoteFolder.getName() + "' created successfully.");
        } catch (URISyntaxException e) {
            e.printStackTrace();
            throw new MakeDirectoryException(e);
        }
    }

    private File gDriveCreateDirectory(CloudDirectory parent, String directoryName) throws URISyntaxException {
        return restTemplate.postForObject(
                UriComponentsBuilder.fromUri(new URI("https", "www.googleapis.com", "/drive/v3/files", "", ""))
                        .toUriString(),
                generateFolderMetadataRequestEntity(new File(), directoryName),
                File.class
        );
    }

    @Override
    public void makeRoot(CloudDirectory root, FuseConfiguration fuseConfiguration) throws MakeRootException {
        logger.info("Mounting root directory...");
        LinkedMultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        ArrayList<String> value = new ArrayList<>();
        value.add("name+=+'" + fuseConfiguration.getDrive().getRemoteFolder() + "'");
        params.put("q", value);
        try {
            File remoteFolder = restTemplate.exchange(
                    UriComponentsBuilder.fromUri(
                            new URI("https", "www.googleapis.com", "/drive/v3/files", "", "")
                    )
                            .queryParams(params).build().toUri(),
                    HttpMethod.GET,
                    generateSearchRequestEntity(),
                    FileList.class
            )
                    .getBody()
                    .getFiles()
                    .stream()
                    .findAny()
                    .orElse(gDriveCreateDirectory(null, fuseConfiguration.getDrive().getRemoteFolder()));
            root.setCloudPathInfo(new GDriveCloudPathInfo(remoteFolder));
        } catch (URISyntaxException e) {
            e.printStackTrace();
            throw new MakeRootException(e);
        }
    }

    private HttpEntity generateSearchRequestEntity() {
        TokenHttpHeaders headers = new TokenHttpHeaders(token);
        return new HttpEntity(headers);
    }

    private HttpEntity<File> generateFileMetadataRequestEntity(File file, String filename) {
        TokenHttpHeaders headers = new TokenHttpHeaders(token);
        return new HttpEntity<>(file.setName(filename), headers);
    }

    private HttpEntity<File> generateFolderMetadataRequestEntity(File file, String filename) {
        TokenHttpHeaders headers = new TokenHttpHeaders(token);
        return new HttpEntity<>(
                file
                        .setName(filename)
                        .setMimeType("application/vnd.google-apps.folder"),
                headers
        );
    }

    private HttpEntity<ByteArrayResource> generateFileCreateRequestEntity(ByteBuffer byteBuffer) {
        ByteArrayResource body = new ByteArrayResource(byteBuffer.array());
        TokenHttpHeaders headers = new TokenHttpHeaders(token);
        headers.add(HttpHeaders.CONTENT_TYPE, "application/octet-stream");
        return new HttpEntity<>(body, headers);
    }

    class TokenHttpHeaders extends HttpHeaders {
        public TokenHttpHeaders(String token) {
            setToken(token);
        }

        void setToken(String token) {
            this.add(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        }
    }
}
