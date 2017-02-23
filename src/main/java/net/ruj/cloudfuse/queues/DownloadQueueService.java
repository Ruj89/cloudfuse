package net.ruj.cloudfuse.queues;

import net.ruj.cloudfuse.clouds.exceptions.DownloadFileException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class DownloadQueueService {
    private final UploadQueueService uploadQueueService;

    @Autowired
    public DownloadQueueService(UploadQueueService uploadQueueService) {
        this.uploadQueueService = uploadQueueService;
    }

    public CompletableFuture<Integer> queueFile(DownloadQueueItem item) {
        ExecutorService executor = Executors.newFixedThreadPool(1);
        return CompletableFuture.supplyAsync(() -> {
            //TODO: Remove possible infinite while cycle
            boolean itemUploading;
            do {
                itemUploading = uploadQueueService.isUploadingFile(item.getFile());
            } while (itemUploading);
            //*************************************************
            try {
                return item.getCloudStorageService().downloadFile(
                        item.getFile(),
                        item.getBytesRead(),
                        item.getOffset(),
                        item.getBytesToRead()
                );
            } catch (DownloadFileException e) {
                e.printStackTrace();
            }
            return 0;
        }, executor);
    }
}
