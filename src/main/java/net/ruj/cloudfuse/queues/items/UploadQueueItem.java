package net.ruj.cloudfuse.queues.items;

import net.ruj.cloudfuse.clouds.CloudStorageService;
import net.ruj.cloudfuse.fuse.filesystem.CloudFile;
import net.ruj.cloudfuse.queues.services.QueueService;

public class UploadQueueItem extends QueueItem {
    private long writeOffset;
    private byte[] bytesToWrite;

    public UploadQueueItem(
            CloudStorageService cloudStorageService,
            QueueService queueService,
            CloudFile file,
            long writeOffset,
            byte[] bytesToWrite
    ) {
        super(cloudStorageService, queueService, file);
        this.writeOffset = writeOffset;
        this.bytesToWrite = bytesToWrite;
    }

    public long getWriteOffset() {
        return writeOffset;
    }

    public void setWriteOffset(long writeOffset) {
        this.writeOffset = writeOffset;
    }

    public byte[] getBytesToWrite() {
        return bytesToWrite;
    }

    public void setBytesToWrite(byte[] bytesToWrite) {
        this.bytesToWrite = bytesToWrite;
    }
}
