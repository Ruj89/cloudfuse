package net.ruj.cloudfuse;

import org.springframework.web.client.RequestCallback;

import java.nio.ByteBuffer;
import java.nio.file.Path;

public interface CloudStorageService {
    void uploadFile(Path path, ByteBuffer byteBuffer);

    RequestCallback generateRequestCallback(Path path, ByteBuffer byteBuffer);
}
