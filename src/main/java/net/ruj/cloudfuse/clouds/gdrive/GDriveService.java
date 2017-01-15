package net.ruj.cloudfuse.clouds.gdrive;

import net.ruj.cloudfuse.clouds.CloudStorageService;
import net.ruj.cloudfuse.clouds.exceptions.*;
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
    private static final String GOOGLE_APPS_FOLDER_MIME_TYPE = "application/vnd.google-apps.folder";
    private final RestTemplate restTemplate;
    private String token;

    GDriveService(String token) {
        this.token = token;
        this.restTemplate = new RestTemplate();
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        restTemplate.setRequestFactory(requestFactory);
    }

    private UriComponentsBuilder getGDriveURIComponentsBuilder(String path) throws URISyntaxException {
        return UriComponentsBuilder.fromUri(
                new URI("https", "www.googleapis.com", path, "", "")
        );
    }

    @Override
    public void createFile(CloudDirectory parent, CloudFile file) throws CreateFileException {
        logger.info("Creating file '" + file.getPath() + "'...");
        try {
            String parentId = ((GDriveCloudPathInfo) parent.getCloudPathInfo()).getLinkedFileInfo().getId();
            File remoteFile = restTemplate.postForObject(
                    this.getGDriveURIComponentsBuilder("/drive/v3/files")
                            .build()
                            .toUri(),
                    generateFileMetadataRequestEntity(
                            new File()
                                    .setName(file.getPath().getFileName().toString())
                                    .addParents(parentId)
                    ),
                    File.class
            );
            file.setCloudPathInfo(new GDriveCloudPathInfo(remoteFile));
        } catch (URISyntaxException e) {
            throw new CreateFileException(e);
        }
    }

    //TODO: Byte streams
    @Override
    public synchronized void uploadFile(CloudFile file) throws UploadFileException {
        logger.info("Updating file '" + file.getPath() + "' content...");
        try {
            String id = ((GDriveCloudPathInfo) file.getCloudPathInfo()).getLinkedFileInfo().getId();
            File remoteFile = restTemplate.postForObject(
                    this.getGDriveURIComponentsBuilder("/upload/drive/v3/files/" + id)
                            .queryParam("uploadType", "media")
                            .build()
                            .toUri(),
                    generateFileUpdateRequestEntity(file.getContents()),
                    File.class
            );
            file.setCloudPathInfo(new GDriveCloudPathInfo(remoteFile));
            logger.info("Upload of file '" + remoteFile.getName() + "' completed.");
        } catch (URISyntaxException e) {
            e.printStackTrace();
            throw new UploadFileException(e);
        }
    }

    @Override
    public void makeDirectory(CloudDirectory parent, CloudDirectory directory) throws MakeDirectoryException {
        logger.info("Making folder '" + directory.getPath() + "'...");
        String parentId = ((GDriveCloudPathInfo) parent.getCloudPathInfo()).getLinkedFileInfo().getId();
        try {
            File remoteFolder = gDriveCreateDirectory(directory.getPath().getFileName().toString(), parentId);
            directory.setCloudPathInfo(new GDriveCloudPathInfo(remoteFolder));
            logger.info("Folder '" + remoteFolder.getName() + "' created successfully.");
        } catch (URISyntaxException e) {
            e.printStackTrace();
            throw new MakeDirectoryException(e);
        }
    }

    //TODO: discover already existent files
    @Override
    public void makeRoot(CloudDirectory root, FuseConfiguration fuseConfiguration) throws MakeRootException {
        logger.info("Mounting root directory...");
        LinkedMultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        ArrayList<String> value = new ArrayList<>();
        value.add("trashed+=+false+and+" +
                "name+=+'" + fuseConfiguration.getDrive().getRemoteFolder() + "'+and+" +
                "'root'+in+parents+and+" +
                "mimeType+=+'" + GOOGLE_APPS_FOLDER_MIME_TYPE + "'");
        params.put("q", value);
        try {
            File remoteFolder = restTemplate.exchange(
                    this.getGDriveURIComponentsBuilder("/drive/v3/files")
                            .queryParams(params)
                            .build()
                            .toUri(),
                    HttpMethod.GET,
                    generateGetRequestEntity(),
                    FileList.class
            )
                    .getBody()
                    .getFiles()
                    .stream()
                    .findAny()
                    .orElseGet(() -> gDriveCreateDirectory(fuseConfiguration.getDrive().getRemoteFolder()));
            root.setCloudPathInfo(new GDriveCloudPathInfo(remoteFolder));
        } catch (URISyntaxException e) {
            e.printStackTrace();
            throw new MakeRootException(e);
        }
    }

    @Override
    public void synchronizeChildrenPaths(CloudDirectory directory) throws SynchronizeChildremException {
        logger.info("Mounting root directory...");

        LinkedMultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        ArrayList<String> value = new ArrayList<>();
        String directoryId = ((GDriveCloudPathInfo) directory.getCloudPathInfo()).getLinkedFileInfo().getId();
        value.add("trashed+=+false+and+" +
                "'" + directoryId + "'+in+parents");
        params.put("q", value);
        try {
            restTemplate.exchange(
                    this.getGDriveURIComponentsBuilder("/drive/v3/files")
                            .queryParams(params)
                            .build()
                            .toUri(),
                    HttpMethod.GET,
                    generateGetRequestEntity(),
                    FileList.class
            )
                    .getBody()
                    .getFiles()
                    .forEach(f -> {
                        if (f.getMimeType().equals(GOOGLE_APPS_FOLDER_MIME_TYPE))
                            synchronizeChildDirectory(directory, f);
                        else
                            synchronizeChildFile(directory, f);
                    });
        } catch (URISyntaxException e) {
            e.printStackTrace();
            throw new SynchronizeChildremException(e);
        }
    }

    @Override
    public void synchronizeFileSize(CloudFile file) throws FileSizeRequestException {
        synchronizeFileInfo(file);
    }

    private void synchronizeFileInfo(CloudFile file) throws FileSizeRequestException {
        try {
            String id = ((GDriveCloudPathInfo) file.getCloudPathInfo()).getLinkedFileInfo().getId();
            File remoteFile = restTemplate.exchange(
                    this.getGDriveURIComponentsBuilder("/drive/v3/files/" + id)
                            .build()
                            .toUri(),
                    HttpMethod.GET,
                    generateGetRequestEntity(),
                    File.class
            ).getBody();
            file.setCloudPathInfo(new GDriveCloudPathInfo(remoteFile));
        } catch (URISyntaxException e) {
            throw new FileSizeRequestException(e);
        }
    }

    private void synchronizeChildFile(CloudDirectory parentDirectory, File file) {
        parentDirectory.mkfile(file.getName(), new GDriveCloudPathInfo(file));
    }

    private void synchronizeChildDirectory(CloudDirectory parentDirectory, File file) {
        parentDirectory.mkdir(file.getName(), new GDriveCloudPathInfo(file));
    }

    private File gDriveCreateDirectory(String directoryName) {
        try {
            return this.gDriveCreateDirectory(directoryName, null);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }

    private File gDriveCreateDirectory(String directoryName, String parentId) throws URISyntaxException {
        File file = new File().setName(directoryName);
        if (parentId != null) file = file.addParents(parentId);
        return restTemplate.postForObject(
                this.getGDriveURIComponentsBuilder("/drive/v3/files")
                        .build()
                        .toUri(),
                generateFolderMetadataRequestEntity(
                        file
                ),
                File.class
        );
    }

    private HttpEntity generateGetRequestEntity() {
        TokenHttpHeaders headers = new TokenHttpHeaders(token);
        return new HttpEntity(headers);
    }

    private HttpEntity<File> generateFileMetadataRequestEntity(File file) {
        TokenHttpHeaders headers = new TokenHttpHeaders(token);
        return new HttpEntity<>(
                file,
                headers
        );
    }

    private HttpEntity<File> generateFolderMetadataRequestEntity(File file) {
        TokenHttpHeaders headers = new TokenHttpHeaders(token);
        return new HttpEntity<>(
                file.setMimeType(GOOGLE_APPS_FOLDER_MIME_TYPE),
                headers
        );
    }

    private HttpEntity<ByteArrayResource> generateFileUpdateRequestEntity(ByteBuffer byteBuffer) {
        ByteArrayResource body = new ByteArrayResource(byteBuffer.array());
        TokenHttpHeaders headers = new TokenHttpHeaders(token);
        headers.add(HttpHeaders.CONTENT_TYPE, "application/octet-stream");
        return new HttpEntity<>(body, headers);
    }

    class TokenHttpHeaders extends HttpHeaders {
        TokenHttpHeaders(String token) {
            setToken(token);
        }

        void setToken(String token) {
            this.add(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        }
    }
}
