package net.ruj.cloudfuse.queues.suppliers;

import net.ruj.cloudfuse.queues.items.QueueItem;
import net.ruj.cloudfuse.queues.items.QueueItemResult;
import net.ruj.cloudfuse.queues.items.QueueItemState;

import java.util.function.Supplier;

public abstract class QueueItemSupplier<T extends QueueItem> implements Supplier<QueueItemResult<T>> {
    final T item;

    QueueItemSupplier(T item) {
        this.item = item;
    }

    @Override
    public QueueItemResult<T> get() {
        item.setState(QueueItemState.STARTED);
        QueueItemResult<T> elaborate = elaborate();
        if (elaborate.getE() != null)
            return elaborate;
        item.setState(QueueItemState.ENDED);
        return elaborate;
    }

    abstract QueueItemResult<T> elaborate();

    public T getItem() {
        return item;
    }
}
