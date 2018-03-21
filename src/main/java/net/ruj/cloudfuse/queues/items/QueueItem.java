package net.ruj.cloudfuse.queues.items;

import net.ruj.cloudfuse.clouds.CloudStorageService;
import net.ruj.cloudfuse.fuse.filesystem.VirtualFile;

public abstract class QueueItem {
    private final CloudStorageService cloudStorageService;
    private final VirtualFile file;
    private QueueItemState state = QueueItemState.ENQUEUED;

    QueueItem(
            CloudStorageService cloudStorageService,
            VirtualFile file
    ) {

        this.cloudStorageService = cloudStorageService;
        this.file = file;
    }

    public CloudStorageService getCloudStorageService() {
        return cloudStorageService;
    }

    public VirtualFile getFile() {
        return file;
    }

    public QueueItemState getState() {
        return state;
    }

    public void setState(QueueItemState state) {
        this.state = state;
    }
}
