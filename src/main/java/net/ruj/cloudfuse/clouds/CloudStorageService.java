package net.ruj.cloudfuse.clouds;

import net.ruj.cloudfuse.fuse.filesystem.CloudDirectory;
import net.ruj.cloudfuse.fuse.filesystem.CloudFile;

public interface CloudStorageService {
    void uploadFile(CloudDirectory parent, CloudFile file);

    void makeDirectory(CloudDirectory parent, CloudDirectory directory);
}
