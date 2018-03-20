package net.ruj.cloudfuse.fuse.filesystem;

import jnr.ffi.Pointer;
import net.ruj.cloudfuse.clouds.exceptions.*;
import net.ruj.cloudfuse.fuse.eventhandlers.FileEventHandler;
import ru.serce.jnrfuse.struct.FileStat;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public class CloudFile extends CloudPath {
    private Set<FileEventHandler> fileEventHandlers = new HashSet<>();

    public CloudFile(Path path, String name, VirtualDirectory parent) {
        super(path, name, parent);
    }

    @Override
    protected void getattr(FileStat stat) {
        stat.st_mode.set(FileStat.S_IFREG | 0777);
        stat.st_size.set(getFileSize());
    }


    int read(Pointer buffer, long size, long offset) {
        int bytesToRead = Math.toIntExact(Math.min(getFileSize() - offset, size));
        byte[] bytesRead = new byte[bytesToRead];
        synchronized (this) {
            download(offset, bytesRead, bytesToRead);
            buffer.put(0, bytesRead, 0, bytesToRead);
        }
        return bytesToRead;
    }

    synchronized void truncate(long size) {
        fileEventHandlers.stream().findAny().map(feh -> {
            try {
                feh.onFileTruncated(this, size);
            } catch (TruncateFileException e) {
                e.printStackTrace();
            }
            return 0;
        });
    }

    int write(Pointer buffer, int bufSize, long writeOffset) {
        byte[] bytesToWrite = new byte[bufSize];
        synchronized (this) {
            buffer.get(0, bytesToWrite, 0, bufSize);
            upload(writeOffset, bytesToWrite);
        }
        return bufSize;
    }

    private void upload(long writeOffset, byte[] bytesToWrite) {
        fileEventHandlers.forEach(feh -> {
            try {
                feh.onFileChanged(this, writeOffset, bytesToWrite);
            } catch (UploadFileException e) {
                e.printStackTrace();
            }
        });
    }

    private void download(long offset, byte[] bytesRead, int bytesToRead) {
        fileEventHandlers.stream().findAny().map(feh -> {
            try {
                return feh.onFileRead(this, bytesRead, offset, bytesToRead);
            } catch (DownloadFileException e) {
                e.printStackTrace();
            }
            return 0;
        });
    }

    @Override
    void remove() {
        fileEventHandlers.forEach(feh -> {
            try {
                feh.onFileRemoved(this);
            } catch (RemoveFileException e) {
                e.printStackTrace();
            }
        });
    }

    private long synchronizeSize() throws NoFileEventsHandlersFoundException, FileSizeRequestException {
        return fileEventHandlers.stream()
                .findAny()
                .orElseThrow(NoFileEventsHandlersFoundException::new)
                .fileSize(this);
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

    private long getFileSize() {
        try {
            return synchronizeSize();
        } catch (NoFileEventsHandlersFoundException | FileSizeRequestException e) {
            e.printStackTrace();
            return 0;
        }
    }
}
