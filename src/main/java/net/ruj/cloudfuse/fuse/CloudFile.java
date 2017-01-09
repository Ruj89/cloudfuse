package net.ruj.cloudfuse.fuse;

import jnr.ffi.Pointer;
import net.ruj.cloudfuse.notifications.eventhandlers.FileEventHandler;
import ru.serce.jnrfuse.struct.FileStat;

import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public class CloudFile extends CloudPath {
    private ByteBuffer contents = ByteBuffer.allocate(0);
    private Set<FileEventHandler> fileEventHandlers = new HashSet<>();

    CloudFile(Path path, String name, CloudDirectory parent) {
        super(path, name, parent);
    }

    @Override
    protected void getattr(FileStat stat) {
        stat.st_mode.set(FileStat.S_IFREG | 0777);
        stat.st_size.set(contents.capacity());
    }

    int read(Pointer buffer, long size, long offset) {
        int bytesToRead = (int) Math.min(contents.capacity() - offset, size);
        byte[] bytesRead = new byte[bytesToRead];
        synchronized (this) {
            contents.position((int) offset);
            contents.get(bytesRead, 0, bytesToRead);
            buffer.put(0, bytesRead, 0, bytesToRead);
            contents.position(0); // Rewind
        }
        return bytesToRead;
    }

    synchronized void truncate(long size) {
        if (size < contents.capacity()) {
            // Need to create a new, smaller buffer
            ByteBuffer newContents = ByteBuffer.allocate((int) size);
            byte[] bytesRead = new byte[(int) size];
            contents.get(bytesRead);
            newContents.put(bytesRead);
            contents = newContents;
        }
    }

    int write(Pointer buffer, long bufSize, long writeOffset) {
        int maxWriteIndex = (int) (writeOffset + bufSize);
        byte[] bytesToWrite = new byte[(int) bufSize];
        synchronized (this) {
            if (maxWriteIndex > contents.capacity()) {
                // Need to create a new, larger buffer
                ByteBuffer newContents = ByteBuffer.allocate(maxWriteIndex);
                newContents.put(contents);
                contents = newContents;
            }
            buffer.get(0, bytesToWrite, 0, (int) bufSize);
            contents.position((int) writeOffset);
            contents.put(bytesToWrite);
            contents.position(0); // Rewind
        }
        changed();
        return (int) bufSize;
    }

    private void changed() {
        fileEventHandlers.forEach(feh -> feh.fileChanged(this));
    }

    public CloudFile addEventHandler(FileEventHandler eventHandler) {
        fileEventHandlers.add(eventHandler);
        return this;
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public ByteBuffer getContents() {
        return contents;
    }

    public void setContents(ByteBuffer contents) {
        this.contents = contents;
    }
}
