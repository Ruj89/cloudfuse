package net.ruj.cloudfuse.gdrive;

import net.ruj.cloudfuse.CloudStorageService;
import net.ruj.cloudfuse.gdrive.models.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;

import java.nio.ByteBuffer;
import java.nio.file.Path;

@Service
public class GDriveService implements CloudStorageService {
    private static final Logger logger = LoggerFactory.getLogger(GDriveService.class);
    private final OAuth2RestTemplate oAuth2RestTemplate;

    @Autowired
    public GDriveService(OAuth2RestTemplate oAuth2RestTemplate) {
        this.oAuth2RestTemplate = oAuth2RestTemplate;
    }

    @Override
    public synchronized void uploadFile(Path path, ByteBuffer byteBuffer) {
        logger.info("Uploading file '" + path + "'...");

        HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = generateRequestEntity(path, byteBuffer);
        logger.info("Upload of file '" + path + "' completed.");
    }

    @Override
    public HttpEntity<LinkedMultiValueMap<String, Object>> generateRequestEntity(Path path, ByteBuffer byteBuffer) {
        HttpHeaders header = new HttpHeaders();
        header.setContentType(MediaType.MULTIPART_FORM_DATA);

        LinkedMultiValueMap<String, Object> multipartRequest = new LinkedMultiValueMap<>();

        HttpHeaders jsonHeaders = new HttpHeaders();
        jsonHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<File> jsonPart = new HttpEntity<>(new File().setName(path.toString()), jsonHeaders);

        HttpHeaders fileHeaders = new HttpHeaders();
        fileHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        ByteArrayResource byteArrayResource = new ByteArrayResource(byteBuffer.array());
        HttpEntity<ByteArrayResource> filePart = new HttpEntity<>(byteArrayResource, fileHeaders);

        // putting the two parts in one request
        multipartRequest.add("data", jsonPart);
        multipartRequest.add("file", filePart);

        return new HttpEntity<>(multipartRequest, header);
    }
}
