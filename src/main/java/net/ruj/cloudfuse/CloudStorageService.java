package net.ruj.cloudfuse;

import org.springframework.http.HttpEntity;
import org.springframework.util.LinkedMultiValueMap;

import java.nio.ByteBuffer;
import java.nio.file.Path;

public interface CloudStorageService {
    void uploadFile(Path path, ByteBuffer byteBuffer);

    HttpEntity<LinkedMultiValueMap<String, Object>> generateRequestEntity(Path path, ByteBuffer byteBuffer);
}
