package net.ruj.cloudfuse.clouds;

import net.ruj.cloudfuse.clouds.exceptions.*;
import net.ruj.cloudfuse.fuse.FuseConfiguration;
import net.ruj.cloudfuse.fuse.filesystem.CloudDirectory;
import net.ruj.cloudfuse.fuse.filesystem.CloudFile;

import java.io.InputStream;

public interface CloudStorageService {
    void createFile(CloudDirectory parent, CloudFile file) throws CreateFileException;

    void uploadFile(CloudFile file) throws UploadFileException;

    InputStream downloadFile(CloudFile file) throws DownloadFileException;

    void makeDirectory(CloudDirectory parent, CloudDirectory directory) throws MakeDirectoryException;

    void makeRoot(CloudDirectory root, FuseConfiguration fuseConfiguration) throws MakeRootException;

    void synchronizeChildrenPaths(CloudDirectory directory) throws SynchronizeChildrenException;

    void synchronizeFileSize(CloudFile file) throws FileSizeRequestException;
}
