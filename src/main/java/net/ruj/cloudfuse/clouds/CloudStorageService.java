package net.ruj.cloudfuse.clouds;

import net.ruj.cloudfuse.clouds.exceptions.*;
import net.ruj.cloudfuse.fuse.FuseConfiguration;
import net.ruj.cloudfuse.fuse.filesystem.CloudDirectory;
import net.ruj.cloudfuse.fuse.filesystem.CloudFile;

public interface CloudStorageService {
    void createFile(CloudDirectory parent, CloudFile file) throws CreateFileException;

    void uploadFile(CloudFile file) throws UploadFileException;

    int downloadFile(CloudFile file, byte[] bytesRead, long offset, int bytesToRead) throws DownloadFileException;

    void truncateFile(CloudFile file, long size) throws TruncateFileException;

    void removeFile(CloudFile file) throws RemoveFileException;

    void makeDirectory(CloudDirectory parent, CloudDirectory directory) throws MakeDirectoryException;

    void removeDirectory(CloudDirectory directory) throws RemoveDirectoryException;

    void makeRoot(CloudDirectory root, FuseConfiguration fuseConfiguration) throws MakeRootException;

    void synchronizeChildrenPaths(CloudDirectory directory) throws SynchronizeChildrenException;

    void synchronizeFileSize(CloudFile file) throws FileSizeRequestException;
}
