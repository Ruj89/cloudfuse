package net.ruj.cloudfuse.queues.suppliers;

import net.ruj.cloudfuse.clouds.exceptions.UploadFileException;
import net.ruj.cloudfuse.queues.items.UploadQueueItem;
import net.ruj.cloudfuse.queues.items.UploadQueueItemResult;

public class UploadQueueItemSupplier extends QueueItemSupplier<UploadQueueItem> {
    public UploadQueueItemSupplier(UploadQueueItem item) {
        super(item);
    }

    @Override
    public UploadQueueItemResult elaborate() {
        try {
            item.getCloudStorageService().uploadFile(
                    item.getFile(),
                    item.getWriteOffset(),
                    item.getBytesToWrite()
            );
        } catch (UploadFileException e) {
            return new UploadQueueItemResult(e);
        }
        return new UploadQueueItemResult(null);
    }
}
