package net.ruj.cloudfuse.queues.items;

public class QueueItemResult<T extends QueueItem> {
    private final Exception e;

    public QueueItemResult(Exception e) {
        this.e = e;
    }

    public Exception getE() {
        return e;
    }
}
