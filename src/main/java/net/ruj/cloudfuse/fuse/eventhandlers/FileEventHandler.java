package net.ruj.cloudfuse.fuse.eventhandlers;

import net.ruj.cloudfuse.clouds.exceptions.DownloadFileException;
import net.ruj.cloudfuse.clouds.exceptions.FileSizeRequestException;
import net.ruj.cloudfuse.clouds.exceptions.UploadFileException;
import net.ruj.cloudfuse.fuse.filesystem.CloudFile;

import java.io.InputStream;

public interface FileEventHandler {
    void fileChanged(CloudFile file) throws UploadFileException;

    InputStream fileRequested(CloudFile cloudFile) throws DownloadFileException;

    long cloudFileSize(CloudFile cloudFile) throws FileSizeRequestException;
}
