package net.ruj.cloudfuse.notifications.eventhandlers;

import net.ruj.cloudfuse.fuse.CloudFile;

public interface FileEventHandler {
    void fileChanged(CloudFile cloudFile);
}
