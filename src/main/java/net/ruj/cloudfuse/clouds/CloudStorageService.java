package net.ruj.cloudfuse.clouds;

import net.ruj.cloudfuse.clouds.exceptions.MakeDirectoryException;
import net.ruj.cloudfuse.clouds.exceptions.MakeRootException;
import net.ruj.cloudfuse.clouds.exceptions.UploadFileException;
import net.ruj.cloudfuse.fuse.FuseConfiguration;
import net.ruj.cloudfuse.fuse.filesystem.CloudDirectory;
import net.ruj.cloudfuse.fuse.filesystem.CloudFile;

public interface CloudStorageService {
    void uploadFile(CloudDirectory parent, CloudFile file) throws UploadFileException;

    void makeDirectory(CloudDirectory parent, CloudDirectory directory) throws MakeDirectoryException;

    void makeRoot(CloudDirectory root, FuseConfiguration fuseConfiguration) throws MakeRootException;
}
