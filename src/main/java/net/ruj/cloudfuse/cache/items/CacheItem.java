package net.ruj.cloudfuse.cache.items;

import net.ruj.cloudfuse.fuse.filesystem.VirtualFile;

public class CacheItem {
    private final VirtualFile file;
    private final byte[] bytes;
    private final long offset;

    public CacheItem(VirtualFile file, byte[] bytes, long offset) {
        this.file = file;
        this.bytes = bytes;
        this.offset = offset;
    }

    public VirtualFile getFile() {
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
