package net.ruj.cloudfuse.queues.items;

import net.ruj.cloudfuse.clouds.CloudStorageService;
import net.ruj.cloudfuse.fuse.filesystem.CloudFile;
import net.ruj.cloudfuse.queues.services.QueueService;

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

    private void notifyState() {
        queueService.queueItemStateChanged(this);
    }

    public CloudStorageService getCloudStorageService() {
        return cloudStorageService;
    }

    public CloudFile getFile() {
        return file;
    }

    public QueueItemState getState() {
        return state;
    }

    public void setState(QueueItemState state) {
        this.state = state;
        notifyState();
    }
}
