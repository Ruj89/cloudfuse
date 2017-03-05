package net.ruj.cloudfuse.queues.suppliers;

import net.ruj.cloudfuse.cache.services.CacheService;
import net.ruj.cloudfuse.clouds.exceptions.UploadFileException;
import net.ruj.cloudfuse.queues.items.DownloadQueueItem;
import net.ruj.cloudfuse.queues.items.UploadQueueItem;
import net.ruj.cloudfuse.queues.items.UploadQueueItemResult;

public class UploadQueueItemSupplier extends QueueItemSupplier<UploadQueueItem> {
    private final CacheService cacheService;

    public UploadQueueItemSupplier(CacheService cacheService, UploadQueueItem item) {
        super(item);
        this.cacheService = cacheService;
    }

    @Override
    public UploadQueueItemResult elaborate() {
        try {
            byte[] bytesRead = getExistentBytes();
            item.getCloudStorageService().uploadFile(
                    item.getFile(),
                    bytesRead,
                    item.getWriteOffset(),
                    item.getBytesToWrite()
            );
        } catch (UploadFileException e) {
            return new UploadQueueItemResult(e);
        }
        return new UploadQueueItemResult(null);
    }

    private byte[] getExistentBytes() {
        int bytesToRead = Math.toIntExact(item.getWriteOffset());
        byte[] bytesRead = new byte[bytesToRead];
        DownloadQueueItemSupplier downloadQueueItemSupplier = new DownloadQueueItemSupplier(
                cacheService,
                generatePartialDownloadQueueItem(
                        bytesToRead,
                        bytesRead
                )
        );
        downloadQueueItemSupplier.elaborate();
        return bytesRead;
    }

    private DownloadQueueItem generatePartialDownloadQueueItem(int bytesToRead, byte[] bytes) {
        return new DownloadQueueItem(
                item.getCloudStorageService(),
                item.getQueueService(),
                item.getFile(),
                bytes,
                0,
                bytesToRead
        );
    }
}
