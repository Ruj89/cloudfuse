package net.ruj.cloudfuse.notifications.eventhandlers;

import net.ruj.cloudfuse.fuse.CloudDirectory;
import net.ruj.cloudfuse.fuse.CloudFile;

public interface DirectoryEventHandler {
    void fileAdded(CloudDirectory directory, CloudFile file);
}
