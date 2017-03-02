package net.ruj.cloudfuse.queues.items;

public abstract class QueueItemResult<T extends QueueItem> {
    private final Exception e;

    QueueItemResult(Exception e) {
        this.e = e;
    }

    public Exception getE() {
        return e;
    }
}
