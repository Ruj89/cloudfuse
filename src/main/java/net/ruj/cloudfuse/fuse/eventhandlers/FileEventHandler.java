package net.ruj.cloudfuse.fuse.eventhandlers;

import net.ruj.cloudfuse.clouds.exceptions.DownloadFileException;
import net.ruj.cloudfuse.clouds.exceptions.FileSizeRequestException;
import net.ruj.cloudfuse.clouds.exceptions.RemoveFileException;
import net.ruj.cloudfuse.clouds.exceptions.UploadFileException;
import net.ruj.cloudfuse.fuse.filesystem.CloudFile;

import java.io.InputStream;

public interface FileEventHandler {
    void onFileChanged(CloudFile file) throws UploadFileException;

    void onFileRemoved(CloudFile cloudFile) throws RemoveFileException;

    InputStream fileInputStream(CloudFile cloudFile) throws DownloadFileException;

    long fileSize(CloudFile cloudFile) throws FileSizeRequestException;
}
