package net.ruj.cloudfuse.services;

import net.ruj.cloudfuse.cache.exceptions.FileNotCachedException;
import net.ruj.cloudfuse.cache.services.CacheService;
import net.ruj.cloudfuse.clouds.CloudStorageService;
import net.ruj.cloudfuse.fuse.filesystem.CloudFile;
import net.ruj.cloudfuse.queues.items.DownloadQueueItem;
import net.ruj.cloudfuse.queues.items.DownloadQueueItemResult;
import net.ruj.cloudfuse.queues.items.QueueItemResult;
import net.ruj.cloudfuse.queues.items.UploadQueueItem;
import net.ruj.cloudfuse.queues.services.QueueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
        queueService.enqueueItem(
                new UploadQueueItem(
                        cloudStorageService,
                        file,
                        writeOffset,
                        bytesToWrite
                )
        );
    }

    public int downloadFile(
            CloudStorageService cloudStorageService,
            CloudFile file,
            byte[] bytesRead,
            long offset,
            int bytesToRead
    ) throws Exception {
        try {
            return cacheService.downloadCachedItem(file, bytesRead, offset, bytesToRead);
        } catch (FileNotCachedException ignore) {
            QueueItemResult result = queueService.enqueueItem(
                    new DownloadQueueItem(
                            cloudStorageService,
                            file,
                            bytesRead,
                            offset,
                            bytesToRead
                    )
            ).get();
            cacheService.storeItemChanges(file, bytesRead, offset);
            return ((DownloadQueueItemResult) result).getFileSize();
        }
    }
}
