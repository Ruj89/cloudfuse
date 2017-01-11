package net.ruj.cloudfuse.clouds.gdrive;

import net.ruj.cloudfuse.clouds.CloudStorageService;
import net.ruj.cloudfuse.fuse.filesystem.CloudDirectory;
import net.ruj.cloudfuse.fuse.filesystem.CloudFile;
import net.ruj.cloudfuse.clouds.gdrive.models.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.nio.ByteBuffer;
import java.nio.file.Path;

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
    public synchronized void uploadFile(CloudDirectory parent, CloudFile file) {
        logger.info("Uploading file '" + file.getPath() + "'...");
        File remoteFile = restTemplate.postForObject(
                "https://www.googleapis.com/upload/drive/v3/files?uploadType=media",
                generateFileCreateRequestEntity(file.getContents()),
                File.class
        );
        remoteFile = restTemplate.patchForObject(
                "https://www.googleapis.com/drive/v3/files/" + remoteFile.getId(),
                generateFileMetadataRequestEntity(remoteFile, file.getPath()),
                File.class
        );
        logger.info("Upload of file '" + remoteFile.getName() + "' completed.");
    }

    //TODO: Handle parents
    @Override
    public void makeDirectory(CloudDirectory parent, CloudDirectory directory) {
        logger.info("Making folder '" + directory.getPath() + "'...");
        File remoteFolder = restTemplate.postForObject(
                "https://www.googleapis.com/drive/v3/files",
                generateFolderMetadataRequestEntity(new File(), directory.getPath()),
                File.class
        );
        logger.info("Folder '" + remoteFolder.getName() + "' created successfully.");
    }

    private HttpEntity<File> generateFileMetadataRequestEntity(File file, Path path) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        return new HttpEntity<>(file.setName(path.getFileName().toString()), headers);
    }

    private HttpEntity<File> generateFolderMetadataRequestEntity(File file, Path path) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        return new HttpEntity<>(
                file
                        .setName(path.getFileName().toString())
                        .setMimeType("application/vnd.google-apps.folder"),
                headers
        );
    }

    private HttpEntity<ByteArrayResource> generateFileCreateRequestEntity(ByteBuffer byteBuffer) {
        ByteArrayResource body = new ByteArrayResource(byteBuffer.array());
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, "application/octet-stream");
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        return new HttpEntity<>(body, headers);
    }
}
