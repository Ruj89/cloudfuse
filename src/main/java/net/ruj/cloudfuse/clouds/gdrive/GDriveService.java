package net.ruj.cloudfuse.clouds.gdrive;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.ruj.cloudfuse.clouds.CloudStorageService;
import net.ruj.cloudfuse.clouds.exceptions.*;
import net.ruj.cloudfuse.clouds.gdrive.models.File;
import net.ruj.cloudfuse.clouds.gdrive.models.FileList;
import net.ruj.cloudfuse.database.services.TokenService;
import net.ruj.cloudfuse.fuse.FuseConfiguration;
import net.ruj.cloudfuse.fuse.exceptions.CloudPathInfoNotFound;
import net.ruj.cloudfuse.fuse.filesystem.VirtualDirectory;
import net.ruj.cloudfuse.fuse.filesystem.VirtualFS;
import net.ruj.cloudfuse.fuse.filesystem.VirtualFile;
import net.ruj.cloudfuse.net.PipeHttpEntity;
import net.ruj.cloudfuse.utils.PatchedInputStream;
import org.apache.commons.io.input.BoundedInputStream;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.persistence.EntityNotFoundException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.springframework.http.HttpHeaders.*;

//TODO: Separate contexts
public class GDriveService implements CloudStorageService {
    private static final Logger logger = LoggerFactory.getLogger(GDriveService.class);
    private static final String GOOGLE_APPS_FOLDER_MIME_TYPE = "application/vnd.google-apps.folder";
    private final RestTemplate restTemplate;
    private final ObjectMapper mapper;
    private TokenService tokenService;

    GDriveService(TokenService tokenService) {
        this.tokenService = tokenService;
        this.restTemplate = new RestTemplate();
        this.mapper = new ObjectMapper();
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        restTemplate.setRequestFactory(requestFactory);
    }

    @Override
    public void init(Path mountPoint, VirtualFS virtualFS) {
        logger.info("Mounting Google Drive fuse partition on '" + mountPoint.toString() + "'...");
        virtualFS.mount(mountPoint, false);
        logger.info("Google Drive mounted!");
    }

    @Override
    public void createFile(VirtualDirectory parent, VirtualFile file) throws CreateFileException {
        logger.info("Creating file '" + file.getPath() + "'...");
        try {
            String parentId = parent.extractPathId();
            File remoteFile = restTemplate.postForObject(
                    this.getGDriveURIComponentsBuilder("/drive/v3/files")
                            .queryParam("fields", getDefaultFileFieldsQueryValue())
                            .build()
                            .toUri(),
                    generateFileMetadataRequestEntity(
                            new File()
                                    .setName(file.getPath().getFileName().toString())
                                    .addParents(parentId)
                    ),
                    File.class
            );
            file.setVirtualPathInfo(new GDriveVirtualPathInfo(remoteFile));
        } catch (URISyntaxException | CloudPathInfoNotFound e) {
            throw new CreateFileException(e);
        }
    }

    @Override
    public synchronized void uploadFile(VirtualFile file, byte[] bytesRead, long writeOffset, byte[] bytesToWrite)
            throws UploadFileException {
        logger.info("Downloading file '" + file.getPath() + "' content...");
        try {
            String id = file.extractPathId();
            HttpClient uploaderClient = HttpClientBuilder.create().build();
            HttpPatch uploaderRequest = new HttpPatch(
                    this.getGDriveURIComponentsBuilder("/upload/drive/v3/files/" + id)
                            .queryParam("uploadType", "media")
                            .queryParam("fields", getDefaultFileFieldsQueryValue())
                            .build()
                            .toUri()
            );
            uploaderRequest.addHeader(AUTHORIZATION, "Bearer " + getTokenString());
            uploaderRequest.addHeader(CONTENT_TYPE, "application/octet-stream");
            try (
                    InputStream downloadedInputStream = new ByteArrayInputStream(bytesRead)
            ) {
                PatchedInputStream patchedInputStream = new PatchedInputStream(
                        downloadedInputStream,
                        bytesToWrite,
                        Math.toIntExact(writeOffset)
                );
                uploaderRequest.setEntity(new PipeHttpEntity(
                        patchedInputStream,
                        patchedInputStream.calculateSize(bytesRead.length)
                ));
                try (
                        InputStream inputStream = uploaderClient.execute(uploaderRequest)
                                .getEntity()
                                .getContent()
                ) {
                    File remoteFile = mapper.readValue(inputStream, File.class);
                    file.setVirtualPathInfo(new GDriveVirtualPathInfo(remoteFile));
                    logger.info("Upload of file '" + remoteFile.getName() + "' completed.");
                }
            }
        } catch (URISyntaxException | IOException | CloudPathInfoNotFound e) {
            e.printStackTrace();
            throw new UploadFileException(e);
        }
    }

    @Override
    public int downloadFile(VirtualFile file, byte[] bytesRead, long offset, int bytesToRead)
            throws DownloadFileException {
        logger.info("Downloading file '" + file.getPath() + "' content...");
        try {
            String id = file.extractPathId();
            HttpClient client = HttpClientBuilder.create().build();
            HttpGet request = new HttpGet(
                    this.getGDriveURIComponentsBuilder("/drive/v3/files/" + id)
                            .queryParam("alt", "media")
                            .build()
                            .toUri()
            );
            request.addHeader(AUTHORIZATION, "Bearer " + getTokenString());
            request.addHeader(RANGE, offset + "-" + (offset + bytesToRead));
            HttpResponse response = client.execute(request);
            try (InputStream is = response.getEntity().getContent()) {
                return is.read(bytesRead, 0, bytesToRead);
            }
        } catch (URISyntaxException | IOException | CloudPathInfoNotFound e) {
            e.printStackTrace();
            throw new DownloadFileException(e);
        }
    }

    @Override
    public void truncateFile(VirtualFile file, long size) throws TruncateFileException {
        logger.info("Downloading file '" + file.getPath() + "' content...");
        try {
            String id = file.extractPathId();
            HttpClient downloaderClient = HttpClientBuilder.create().build();
            HttpGet downloaderRequest = new HttpGet(
                    this.getGDriveURIComponentsBuilder("/drive/v3/files/" + id)
                            .queryParam("alt", "media")
                            .build()
                            .toUri()
            );
            downloaderRequest.addHeader(AUTHORIZATION, "Bearer " + getTokenString());
            HttpResponse downloaderResponse = downloaderClient.execute(downloaderRequest);
            try (InputStream is = new BoundedInputStream(downloaderResponse.getEntity().getContent(), size)) {
                HttpClient uploaderClient = HttpClientBuilder.create().build();
                HttpPatch uploaderRequest = new HttpPatch(
                        this.getGDriveURIComponentsBuilder("/upload/drive/v3/files/" + id)
                                .queryParam("uploadType", "media")
                                .queryParam("fields", getDefaultFileFieldsQueryValue())
                                .build()
                                .toUri()
                );
                uploaderRequest.addHeader(AUTHORIZATION, "Bearer " + getTokenString());
                uploaderRequest.addHeader(CONTENT_TYPE, "application/octet-stream");
                uploaderRequest.setEntity(new PipeHttpEntity(is, size));
                try (InputStream inputStream = uploaderClient.execute(uploaderRequest).getEntity().getContent()) {
                    File remoteFile = mapper.readValue(inputStream, File.class);
                    file.setVirtualPathInfo(new GDriveVirtualPathInfo(remoteFile));
                    logger.info("Upload of file '" + remoteFile.getName() + "' completed.");
                }
            }
        } catch (URISyntaxException | IOException | CloudPathInfoNotFound e) {
            e.printStackTrace();
            throw new TruncateFileException(e);
        }
    }

    @Override
    public void removeFile(VirtualFile file) throws RemoveFileException {
        logger.info("Removing file '" + file.getPath() + "'");
        try {
            String id = file.extractPathId();
            restTemplate.exchange(
                    this.getGDriveURIComponentsBuilder("/drive/v3/files/" + id)
                            .build()
                            .toUri(),
                    HttpMethod.DELETE,
                    generateDeleteRequestEntity(),
                    FileList.class
            );
        } catch (URISyntaxException | CloudPathInfoNotFound e) {
            e.printStackTrace();
            throw new RemoveFileException(e);
        }
    }

    @Override
    public void makeDirectory(VirtualDirectory parent, VirtualDirectory directory) throws MakeDirectoryException {
        logger.info("Making folder '" + directory.getPath() + "'...");
        try {
            String parentId = parent.extractPathId();
            File remoteFolder = gDriveCreateDirectory(directory.getPath().getFileName().toString(), parentId);
            directory.setVirtualPathInfo(new GDriveVirtualPathInfo(remoteFolder));
            logger.info("Folder '" + remoteFolder.getName() + "' created successfully.");
        } catch (URISyntaxException | CloudPathInfoNotFound e) {
            e.printStackTrace();
            throw new MakeDirectoryException(e);
        }
    }

    @Override
    public void removeDirectory(VirtualDirectory directory) throws RemoveDirectoryException {
        logger.info("Removing directory '" + directory.getPath() + "'");
        try {
            String id = directory.extractPathId();
            restTemplate.exchange(
                    this.getGDriveURIComponentsBuilder("/drive/v3/files/" + id)
                            .build()
                            .toUri(),
                    HttpMethod.DELETE,
                    generateDeleteRequestEntity(),
                    FileList.class
            );
        } catch (URISyntaxException | CloudPathInfoNotFound e) {
            e.printStackTrace();
            throw new RemoveDirectoryException(e);
        }
    }

    @Override
    public void makeRoot(VirtualDirectory root, FuseConfiguration fuseConfiguration) throws MakeRootException {
        logger.info("Mounting root directory...");
        LinkedMultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.put("q", Collections.singletonList(
                "trashed+=+false+and+" +
                        "name+=+'" + fuseConfiguration.getDrive().getRemoteFolder() + "'+and+" +
                        "'root'+in+parents+and+" +
                        "mimeType+=+'" + GOOGLE_APPS_FOLDER_MIME_TYPE + "'"
        ));
        params.put("fields", Collections.singletonList("files(" + getDefaultFileFieldsQueryValue() + ")"));
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
            root.setVirtualPathInfo(new GDriveVirtualPathInfo(remoteFolder));
            //TODO: Transactional synchronization
            root.synchronizeChildrenPaths();
        } catch (URISyntaxException e) {
            e.printStackTrace();
            throw new MakeRootException(e);
        }
    }

    @Override
    public void synchronizeChildrenPaths(VirtualDirectory directory) throws SynchronizeChildrenException {
        logger.info("Synchronizing directory...");

        try {
            LinkedMultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            String directoryId = directory.extractPathId();
            params.put("q", Collections.singletonList("trashed+=+false+and+'" + directoryId + "'+in+parents"));
            params.put("fields", Collections.singletonList("files(" + getDefaultFileFieldsQueryValue() + ")"));
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
        } catch (URISyntaxException | CloudPathInfoNotFound e) {
            e.printStackTrace();
            throw new SynchronizeChildrenException(e);
        }
    }

    @Override
    public void synchronizeFileSize(VirtualFile file) throws FileSizeRequestException {
        synchronizeFileInfo(file);
    }

    @Override
    public boolean isReady() {
        try {
            getTokenString();
            return true;
        } catch (EntityNotFoundException e) {
            return false;
        }
    }

    private String getTokenString() {
        return tokenService.getTokenString();
    }

    private UriComponentsBuilder getGDriveURIComponentsBuilder(String path) throws URISyntaxException {
        return UriComponentsBuilder.fromUri(
                new URI("https", "www.googleapis.com", path, "", "")
        );
    }

    private void synchronizeFileInfo(VirtualFile file) throws FileSizeRequestException {
        try {
            String id = file.extractPathId();
            File remoteFile = restTemplate.exchange(
                    this.getGDriveURIComponentsBuilder("/drive/v3/files/" + id)
                            .queryParam("fields", getDefaultFileFieldsQueryValue())
                            .build()
                            .toUri(),
                    HttpMethod.GET,
                    generateGetRequestEntity(),
                    File.class
            ).getBody();
            file.setVirtualPathInfo(new GDriveVirtualPathInfo(remoteFile));
        } catch (URISyntaxException | CloudPathInfoNotFound e) {
            throw new FileSizeRequestException(e);
        }
    }

    private void synchronizeChildFile(VirtualDirectory parentDirectory, File file) {
        parentDirectory.mkfile(file.getName(), new GDriveVirtualPathInfo(file));
    }

    private void synchronizeChildDirectory(VirtualDirectory parentDirectory, File file) {
        parentDirectory.mkdir(file.getName(), new GDriveVirtualPathInfo(file));
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
                        .queryParam("fields", getDefaultFileFieldsQueryValue())
                        .build()
                        .toUri(),
                generateFolderMetadataRequestEntity(
                        file
                ),
                File.class
        );
    }

    private HttpEntity generateGetRequestEntity() {
        TokenHttpHeaders headers = new TokenHttpHeaders(getTokenString());
        return new HttpEntity(headers);
    }

    private HttpEntity generateDeleteRequestEntity() {
        TokenHttpHeaders headers = new TokenHttpHeaders(getTokenString());
        return new HttpEntity(headers);
    }

    private HttpEntity<File> generateFileMetadataRequestEntity(File file) {
        TokenHttpHeaders headers = new TokenHttpHeaders(getTokenString());
        return new HttpEntity<>(
                file,
                headers
        );
    }

    private HttpEntity<File> generateFolderMetadataRequestEntity(File file) {
        TokenHttpHeaders headers = new TokenHttpHeaders(getTokenString());
        return new HttpEntity<>(
                file.setMimeType(GOOGLE_APPS_FOLDER_MIME_TYPE),
                headers
        );
    }

    private String getDefaultFileFieldsQueryValue() {
        return Stream.of(
                "id",
                "name",
                "kind",
                "mimeType",
                "parents",
                "size"
        )
                .collect(Collectors.joining(","));
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
