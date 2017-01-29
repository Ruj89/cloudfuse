package net.ruj.cloudfuse.utils;

import java.io.IOException;
import java.io.InputStream;

public class PatchedInputStream extends InputStream {
    private InputStream patchedInputStream;
    private byte[] patchingBytes;
    private int offset;
    private int length;
    private int position = 0;

    public PatchedInputStream(InputStream patchedInputStream, byte[] patchingBytes, int offset, int length) {
        this.patchedInputStream = patchedInputStream;
        this.patchingBytes = patchingBytes;
        this.offset = offset;
        this.length = length;
    }

    public PatchedInputStream(InputStream downloadedInputStream, byte[] patchingBytes, int offset) {
        this(downloadedInputStream, patchingBytes, offset, patchingBytes.length);
    }

    @Override
    public int read() throws IOException {
        int returningByte = -1;
        try {
            if (position < offset || position >= offset + length)
                returningByte = this.patchedInputStream.read();
            else {
                //noinspection ResultOfMethodCallIgnored
                this.patchedInputStream.read();
                returningByte = patchingBytes[position - offset] & 0xff;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        position++;
        return returningByte;
    }

    @Override
    public synchronized void reset() throws IOException {
        this.patchedInputStream.reset();
        this.position = 0;
    }

    public long calculateSize(long inputStreamSize) {
        long size = inputStreamSize + length;
        if (offset < inputStreamSize) {
            long insideOldSize = inputStreamSize - offset;
            if (insideOldSize > length)
                insideOldSize = length;
            size -= insideOldSize;
        }
        return size;
    }
}
