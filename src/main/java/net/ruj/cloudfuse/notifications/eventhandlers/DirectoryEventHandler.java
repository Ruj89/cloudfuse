package net.ruj.cloudfuse.notifications.eventhandlers;

import net.ruj.cloudfuse.fuse.CloudDirectory;
import net.ruj.cloudfuse.fuse.CloudFile;

public interface DirectoryEventHandler {
    void fileAdded(CloudDirectory parent, CloudFile file);

    void directoryAdded(CloudDirectory parent, CloudDirectory directory);
}
