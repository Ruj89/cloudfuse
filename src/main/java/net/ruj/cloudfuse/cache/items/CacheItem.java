package net.ruj.cloudfuse.cache.items;

import net.ruj.cloudfuse.fuse.filesystem.CloudFile;
import net.ruj.cloudfuse.queues.items.QueueItem;

public class CacheItem {
    private final CloudFile file;
    private final byte[] bytes;
    private final long offset;

    public CacheItem(CloudFile file, byte[] bytes, long offset) {
        this.file = file;
        this.bytes = bytes;
        this.offset = offset;
    }

    public CacheItem(QueueItem queueItem, byte[] bytes, long offset) {
        this(queueItem.getFile(), bytes, offset);
    }

    public CloudFile getFile() {
        return file;
    }

    public long getOffset() {
        return offset;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public boolean containsRange(long start, long length) {
        return offset <= start && bytes.length - (start - offset) >= length;
    }
}
