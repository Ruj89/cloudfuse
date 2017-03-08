package net.ruj.cloudfuse.services;

import net.ruj.cloudfuse.cache.services.CacheService;
import net.ruj.cloudfuse.clouds.CloudStorageService;
import net.ruj.cloudfuse.fuse.filesystem.CloudFile;
import net.ruj.cloudfuse.queues.exceptions.WrongQueueItemResultTypeException;
import net.ruj.cloudfuse.queues.items.*;
import net.ruj.cloudfuse.queues.services.QueueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class AggregatorService {
    private final CacheService cacheService;
    private final QueueService queueService;

    @Autowired
    public AggregatorService(CacheService cacheService, QueueService queueService) {
        this.cacheService = cacheService;
        this.queueService = queueService;
    }

    public void changeFile(
            CloudStorageService cloudStorageService,
            CloudFile file,
            long writeOffset,
            byte[] bytesToWrite
    ) throws Exception {
        cacheService.storeItemChanges(file, bytesToWrite, writeOffset);
        QueueItemResult result = queueService.enqueueFile(
                new UploadQueueItem(
                        cloudStorageService,
                        queueService,
                        file,
                        writeOffset,
                        bytesToWrite)
        ).get();
        if (result.getE() != null)
            throw result.getE();
        if (!(result instanceof UploadQueueItemResult))
            throw new WrongQueueItemResultTypeException();
    }

    public int downloadFile(
            CloudStorageService cloudStorageService,
            CloudFile file,
            byte[] bytesRead,
            long offset,
            int bytesToRead
    ) throws Exception {
        CompletableFuture<? extends QueueItemResult> futureTask = queueService.enqueueFile(
                new DownloadQueueItem(
                        cloudStorageService,
                        queueService,
                        file,
                        bytesRead,
                        offset,
                        bytesToRead
                )
        );
        QueueItemResult result = futureTask.get();
        if (result.getE() != null)
            throw result.getE();
        if (!(result instanceof DownloadQueueItemResult))
            throw new WrongQueueItemResultTypeException();

        return ((DownloadQueueItemResult) result).getFileSize();
    }
}
