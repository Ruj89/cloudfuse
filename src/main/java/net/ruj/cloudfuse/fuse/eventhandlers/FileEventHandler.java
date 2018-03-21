package net.ruj.cloudfuse.fuse.eventhandlers;

import net.ruj.cloudfuse.clouds.exceptions.*;
import net.ruj.cloudfuse.fuse.filesystem.VirtualFile;

public interface FileEventHandler {
    void onFileChanged(VirtualFile file, long writeOffset, byte[] bytesToWrite) throws UploadFileException;

    void onFileRemoved(VirtualFile cloudFile) throws RemoveFileException;

    int onFileRead(VirtualFile cloudFile, byte[] bytesRead, long offset, int bytesToRead) throws DownloadFileException;

    long fileSize(VirtualFile cloudFile) throws FileSizeRequestException;

    void onFileTruncated(VirtualFile cloudFile, long size) throws TruncateFileException;
}
