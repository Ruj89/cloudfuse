package net.ruj.cloudfuse.queues.suppliers;

import net.ruj.cloudfuse.clouds.exceptions.DownloadFileException;
import net.ruj.cloudfuse.queues.items.DownloadQueueItem;
import net.ruj.cloudfuse.queues.items.DownloadQueueItemResult;

public class DownloadQueueItemSupplier extends QueueItemSupplier<DownloadQueueItem> {
    public DownloadQueueItemSupplier(DownloadQueueItem item) {
        super(item);
    }

    @Override
    public DownloadQueueItemResult elaborate() {
        try {
            return new DownloadQueueItemResult(
                    item.getCloudStorageService().downloadFile(
                            item.getFile(),
                            item.getBytesRead(),
                            item.getOffset(),
                            item.getBytesToRead()
                    ),
                    null
            );
        } catch (DownloadFileException e) {
            return new DownloadQueueItemResult(0, e);
        }
    }
}
