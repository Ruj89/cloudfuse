package net.ruj.cloudfuse.queues.items;

import net.ruj.cloudfuse.clouds.CloudStorageService;
import net.ruj.cloudfuse.fuse.filesystem.CloudFile;

public abstract class QueueItem {
    private final CloudStorageService cloudStorageService;
    private final CloudFile file;
    private QueueItemState state = QueueItemState.ENQUEUED;

    QueueItem(
            CloudStorageService cloudStorageService,
            CloudFile file) {

        this.cloudStorageService = cloudStorageService;
        this.file = file;
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
    }
}
