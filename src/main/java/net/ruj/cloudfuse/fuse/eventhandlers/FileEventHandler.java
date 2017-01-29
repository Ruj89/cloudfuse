package net.ruj.cloudfuse.fuse.eventhandlers;

import net.ruj.cloudfuse.clouds.exceptions.*;
import net.ruj.cloudfuse.fuse.filesystem.CloudFile;

public interface FileEventHandler {
    void onFileChanged(CloudFile file, long writeOffset, byte[] bytesToWrite) throws UploadFileException;

    void onFileRemoved(CloudFile cloudFile) throws RemoveFileException;

    int onFileRead(CloudFile cloudFile, byte[] bytesRead, long offset, int bytesToRead) throws DownloadFileException;

    long fileSize(CloudFile cloudFile) throws FileSizeRequestException;

    void onFileTruncated(CloudFile cloudFile, long size) throws TruncateFileException;
}
