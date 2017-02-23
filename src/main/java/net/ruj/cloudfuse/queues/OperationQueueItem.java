package net.ruj.cloudfuse.queues;

import net.ruj.cloudfuse.clouds.CloudStorageService;
import net.ruj.cloudfuse.fuse.filesystem.CloudFile;

public class OperationQueueItem {
    private final CloudStorageService cloudStorageService;
    private final CloudFile file;

    public OperationQueueItem(
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
}
