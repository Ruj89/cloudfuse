package net.ruj.cloudfuse.fuse.eventhandlers;

import net.ruj.cloudfuse.fuse.filesystem.CloudDirectory;
import net.ruj.cloudfuse.fuse.filesystem.CloudFile;

public interface DirectoryEventHandler {
    void fileAdded(CloudDirectory parent, CloudFile file);

    void directoryAdded(CloudDirectory parent, CloudDirectory directory);
}
