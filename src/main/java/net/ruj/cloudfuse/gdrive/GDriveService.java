package net.ruj.cloudfuse.gdrive;

import net.ruj.cloudfuse.CloudStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.HttpMessageConverterExtractor;
import org.springframework.web.client.RequestCallback;
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
    }

    @Override
    public synchronized void uploadFile(Path path, ByteBuffer byteBuffer) {
        logger.info("Uploading file '" + path + "'...");

        final RequestCallback requestCallback = generateRequestCallback(path, byteBuffer);
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setBufferRequestBody(false);
        restTemplate.setRequestFactory(requestFactory);
        final HttpMessageConverterExtractor<String> responseExtractor =
                new HttpMessageConverterExtractor<>(String.class, restTemplate.getMessageConverters());
        restTemplate.execute(
                "https://www.googleapis.com/upload/drive/v3/files?uploadType=media",
                HttpMethod.POST,
                requestCallback,
                responseExtractor
        );

        logger.info("Upload of file '" + path + "' completed.");
    }

    @Override
    public RequestCallback generateRequestCallback(Path path, ByteBuffer byteBuffer) {
        return request -> {
            request.getHeaders().add(HttpHeaders.CONTENT_TYPE, "application/octet-stream");
            request.getHeaders().add(HttpHeaders.AUTHORIZATION, "Bearer " + token);
            request.getBody().write(byteBuffer.array());
        };
    }
}
