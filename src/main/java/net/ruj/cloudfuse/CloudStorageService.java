package net.ruj.cloudfuse;

import net.ruj.cloudfuse.fuse.CloudDirectory;
import net.ruj.cloudfuse.fuse.CloudFile;

public interface CloudStorageService {
    void uploadFile(CloudDirectory parent, CloudFile file);

    void makeDirectory(CloudDirectory parent, CloudDirectory directory);
}
