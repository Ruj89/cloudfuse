package net.ruj.cloudfuse.queues.items;

public class DownloadQueueItemResult extends QueueItemResult<DownloadQueueItem> {
    private final int fileSize;

    public DownloadQueueItemResult(int fileSize, Exception e) {
        super(e);
        this.fileSize = fileSize;
    }

    public int getFileSize() {
        return fileSize;
    }
}
