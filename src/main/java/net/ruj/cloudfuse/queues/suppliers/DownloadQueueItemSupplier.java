package net.ruj.cloudfuse.queues.suppliers;

import net.ruj.cloudfuse.cache.exceptions.CacheItemAbsentOrIncompatible;
import net.ruj.cloudfuse.cache.items.CacheItem;
import net.ruj.cloudfuse.cache.services.CacheService;
import net.ruj.cloudfuse.clouds.exceptions.DownloadFileException;
import net.ruj.cloudfuse.queues.items.DownloadQueueItem;
import net.ruj.cloudfuse.queues.items.DownloadQueueItemResult;

import java.util.Optional;

public class DownloadQueueItemSupplier extends QueueItemSupplier<DownloadQueueItem> {
    private final CacheService cacheService;

    public DownloadQueueItemSupplier(CacheService cacheService, DownloadQueueItem item) {
        super(item);
        this.cacheService = cacheService;
    }

    @Override
    public DownloadQueueItemResult elaborate() {
        try {
            try {
                return getFromCache();
            } catch (CacheItemAbsentOrIncompatible e) {
                return downloadFromCloud();
            }
        } catch (DownloadFileException e) {
            return new DownloadQueueItemResult(0, e);
        }
    }

    private DownloadQueueItemResult downloadFromCloud() throws DownloadFileException {
        return new DownloadQueueItemResult(
                item.getCloudStorageService().downloadFile(
                        item.getFile(),
                        item.getBytesRead(),
                        item.getOffset(),
                        item.getBytesToRead()
                ),
                null
        );
    }

    private DownloadQueueItemResult getFromCache() throws DownloadFileException, CacheItemAbsentOrIncompatible {
        Optional<CacheItem> cachedItemO = cacheService.contains(
                item.getFile(),
                item.getOffset(),
                item.getBytesToRead()
        );
        if (cachedItemO.isPresent()) {
            CacheItem cachedItem = cachedItemO.get();
            return new DownloadQueueItemResult(
                    cacheService.downloadCachedItem(
                            cachedItem,
                            item.getBytesRead(),
                            item.getOffset(),
                            item.getBytesToRead()
                    ),
                    null
            );
        }
        throw new CacheItemAbsentOrIncompatible();
    }
}
