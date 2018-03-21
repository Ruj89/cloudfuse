package net.ruj.cloudfuse.queues.items;

import net.ruj.cloudfuse.clouds.CloudStorageService;
import net.ruj.cloudfuse.fuse.filesystem.VirtualFile;

public class DownloadQueueItem extends QueueItem {
    private byte[] bytesRead;
    private long offset;
    private int bytesToRead;

    public DownloadQueueItem(
            CloudStorageService cloudStorageService,
            VirtualFile file,
            byte[] bytesRead,
            long offset,
            int bytesToRead
    ) {
        super(cloudStorageService, file);
        this.bytesRead = bytesRead;
        this.offset = offset;
        this.bytesToRead = bytesToRead;
    }

    public byte[] getBytesRead() {
        return bytesRead;
    }

    public void setBytesRead(byte[] bytesRead) {
        this.bytesRead = bytesRead;
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public int getBytesToRead() {
        return bytesToRead;
    }

    public void setBytesToRead(int bytesToRead) {
        this.bytesToRead = bytesToRead;
    }
}
