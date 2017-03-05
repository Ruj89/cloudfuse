package net.ruj.cloudfuse.queues.suppliers;

import net.ruj.cloudfuse.cache.exceptions.BiasedStartingOffsetItemException;
import net.ruj.cloudfuse.queues.items.QueueItem;
import net.ruj.cloudfuse.queues.items.QueueItemResult;
import net.ruj.cloudfuse.queues.items.QueueItemState;

import java.io.IOException;
import java.util.function.Supplier;

public abstract class QueueItemSupplier<T extends QueueItem> implements Supplier<QueueItemResult<T>> {
    final T item;

    QueueItemSupplier(T item) {
        this.item = item;
    }

    @Override
    public QueueItemResult<T> get() {
        try {
            item.setState(QueueItemState.STARTED);
        } catch (IOException | BiasedStartingOffsetItemException e) {
            return new QueueItemResult<T>(e);
        }
        QueueItemResult<T> elaborate = elaborate();
        if (elaborate.getE() != null)
            return elaborate;
        try {
            item.setState(QueueItemState.ENDED);
        } catch (IOException | BiasedStartingOffsetItemException e) {
            return new QueueItemResult<T>(e);
        }
        return elaborate;
    }

    abstract QueueItemResult<T> elaborate();

    public T getItem() {
        return item;
    }
}
