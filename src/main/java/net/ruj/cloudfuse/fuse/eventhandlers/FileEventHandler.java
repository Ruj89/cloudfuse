package net.ruj.cloudfuse.fuse.eventhandlers;

import net.ruj.cloudfuse.clouds.exceptions.UploadFileException;
import net.ruj.cloudfuse.fuse.filesystem.CloudFile;

public interface FileEventHandler {
    void fileChanged(CloudFile file) throws UploadFileException;
}
