package net.ruj.cloudfuse.notifications.eventhandlers;

import net.ruj.cloudfuse.fuse.CloudDirectory;
import net.ruj.cloudfuse.fuse.CloudFile;

public interface FileEventHandler {
    void fileChanged(CloudDirectory parent, CloudFile file);
}
