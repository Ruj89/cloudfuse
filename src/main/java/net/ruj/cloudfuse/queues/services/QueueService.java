package net.ruj.cloudfuse.queues.services;

import net.ruj.cloudfuse.queues.exceptions.ItemTypeNotServedException;
import net.ruj.cloudfuse.queues.items.*;
import net.ruj.cloudfuse.queues.suppliers.DownloadQueueItemSupplier;
import net.ruj.cloudfuse.queues.suppliers.QueueItemSupplier;
import net.ruj.cloudfuse.queues.suppliers.UploadQueueItemSupplier;
import org.apache.commons.collections4.list.TreeList;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class QueueService {
    private TreeList<QueueItemSupplier> items = new TreeList<>();
    private ExecutorService executor = Executors.newFixedThreadPool(1);

    public CompletableFuture<? extends QueueItemResult> enqueueFile(QueueItem item) throws ItemTypeNotServedException {
        QueueItemSupplier<? extends QueueItem> supplier = generateSupplierByItemType(item);
        items.add(supplier);
        return CompletableFuture.supplyAsync(supplier, executor);
    }

    private QueueItemSupplier<? extends QueueItem> generateSupplierByItemType(final QueueItem item)
            throws ItemTypeNotServedException {
        if (item instanceof UploadQueueItem)
            return generateUploadQueueItemSupplier((UploadQueueItem) item);
        else if (item instanceof DownloadQueueItem)
            return generateDownloadItemQueueSupplier((DownloadQueueItem) item);
        throw new ItemTypeNotServedException();
    }

    private UploadQueueItemSupplier generateUploadQueueItemSupplier(UploadQueueItem item) {
        final UploadQueueItem finalItem = item;
        UploadQueueItemSupplier appendingSupplier = items.stream()
                .filter(is -> is instanceof UploadQueueItemSupplier)
                .map(i -> (UploadQueueItemSupplier) i)
                .filter(i -> i.getItem().getState().equals(QueueItemState.ENQUEUED))
                .filter(i -> canAppendItem(i, finalItem))
                .findAny()
                .orElseGet(() -> null);
        if (appendingSupplier != null) {
            UploadQueueItem appendingSupplierItem = appendingSupplier.getItem();
            appendingSupplierItem.setBytesToWrite(ArrayUtils.addAll(
                    item.getBytesToWrite(),
                    appendingSupplierItem.getBytesToWrite()
            ));
            return appendingSupplier;
        } else
            return new UploadQueueItemSupplier(item);
    }

    private boolean canAppendItem(UploadQueueItemSupplier uqis, UploadQueueItem finalItem) {
        UploadQueueItem i = uqis.getItem();
        return i.getWriteOffset() == finalItem.getWriteOffset() + finalItem.getBytesToWrite().length &&
                i.getFile().equals(finalItem.getFile()) &&
                i.getCloudStorageService().equals(finalItem.getCloudStorageService());
    }

    private DownloadQueueItemSupplier generateDownloadItemQueueSupplier(DownloadQueueItem item) {
        return new DownloadQueueItemSupplier(item);
    }
}
