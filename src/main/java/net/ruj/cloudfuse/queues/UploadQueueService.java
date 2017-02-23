package net.ruj.cloudfuse.queues;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import net.ruj.cloudfuse.clouds.exceptions.UploadFileException;
import net.ruj.cloudfuse.fuse.filesystem.CloudFile;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class UploadQueueService {
    private LinkedList<UploadQueueItem> items = new LinkedList<>();
    private LinkedList<UploadQueueItem> elaboratingItems = new LinkedList<>();
    private ExecutorService executor = Executors.newFixedThreadPool(1);

    public UploadQueueService() {
        ObservableList<UploadQueueItem> observableList = FXCollections.observableList(items);
        observableList.addListener((ListChangeListener<UploadQueueItem>) c -> {
            if (c.wasAdded())
                executor.submit(() -> {
                    try {
                        elaborateItem();
                    } catch (UploadFileException e) {
                        e.printStackTrace();
                    }
                });
        });
    }

    public void queueFile(UploadQueueItem item) {
        items.push(item);
    }

    private void elaborateItem() throws UploadFileException, NoSuchElementException {
        this.elaboratingItems.push(items.getFirst());
        UploadQueueItem item = items.pop();
        item = appendConcatenatedItems(item);
        item.getCloudStorageService().uploadFile(
                item.getFile(),
                item.getWriteOffset(),
                item.getBytesToWrite()
        );
        this.elaboratingItems.remove(item);
    }

    private UploadQueueItem appendConcatenatedItems(UploadQueueItem item) {
        UploadQueueItem appendingItem;
        do {
            final UploadQueueItem finalItem = item;
            appendingItem = items.stream()
                    .filter(i -> i.getWriteOffset() == finalItem.getWriteOffset() + finalItem.getBytesToWrite().length)
                    .filter(i -> i.getFile().equals(finalItem.getFile()))
                    .filter(i -> i.getCloudStorageService().equals(finalItem.getCloudStorageService()))
                    .findAny()
                    .orElseGet(() -> null);
            if (appendingItem != null) {
                items.remove(appendingItem);
                item = new UploadQueueItem(
                        item.getCloudStorageService(),
                        item.getFile(),
                        item.getWriteOffset(),
                        ArrayUtils.addAll(item.getBytesToWrite(), appendingItem.getBytesToWrite())
                );
            }
        } while (appendingItem != null);
        return item;
    }

    boolean isUploadingFile(CloudFile file) {
        return items.stream()
                .map(UploadQueueItem::getFile)
                .anyMatch(f -> f.equals(file))
                ||
                elaboratingItems.stream()
                        .map(UploadQueueItem::getFile)
                        .anyMatch(f -> f.equals(file));
    }
}
