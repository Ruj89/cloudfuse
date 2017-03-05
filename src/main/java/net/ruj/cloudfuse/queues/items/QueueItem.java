package net.ruj.cloudfuse.queues.items;

import net.ruj.cloudfuse.cache.exceptions.BiasedStartingOffsetItemException;
import net.ruj.cloudfuse.clouds.CloudStorageService;
import net.ruj.cloudfuse.fuse.filesystem.CloudFile;
import net.ruj.cloudfuse.queues.services.QueueService;

import java.io.IOException;

public abstract class QueueItem {
    private final CloudStorageService cloudStorageService;
    private final QueueService queueService;
    private final CloudFile file;
    private QueueItemState state = QueueItemState.ENQUEUED;

    QueueItem(
            CloudStorageService cloudStorageService,
            QueueService queueService, CloudFile file) {

        this.cloudStorageService = cloudStorageService;
        this.queueService = queueService;
        this.file = file;
    }

    private void notifyState() throws IOException, BiasedStartingOffsetItemException {
        queueService.queueItemStateChanged(this);
    }

    public CloudStorageService getCloudStorageService() {
        return cloudStorageService;
    }

    public QueueService getQueueService() {
        return queueService;
    }

    public CloudFile getFile() {
        return file;
    }

    public QueueItemState getState() {
        return state;
    }

    public void setState(QueueItemState state) throws IOException, BiasedStartingOffsetItemException {
        this.state = state;
        notifyState();
    }
}
