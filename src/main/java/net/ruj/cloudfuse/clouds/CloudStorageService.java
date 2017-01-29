package net.ruj.cloudfuse.clouds;

import net.ruj.cloudfuse.clouds.exceptions.*;
import net.ruj.cloudfuse.fuse.FuseConfiguration;
import net.ruj.cloudfuse.fuse.filesystem.CloudDirectory;
import net.ruj.cloudfuse.fuse.filesystem.CloudFS;
import net.ruj.cloudfuse.fuse.filesystem.CloudFile;

import java.nio.file.Path;

public interface CloudStorageService {
    void init(Path mountPoint, CloudFS cloudFS) throws MakeRootException;

    void createFile(CloudDirectory parent, CloudFile file) throws CreateFileException;

    void uploadFile(CloudFile file, long writeOffset, byte[] bytesToWrite) throws UploadFileException;

    int downloadFile(CloudFile file, byte[] bytesRead, long offset, int bytesToRead) throws DownloadFileException;

    void truncateFile(CloudFile file, long size) throws TruncateFileException;

    void removeFile(CloudFile file) throws RemoveFileException;

    void makeDirectory(CloudDirectory parent, CloudDirectory directory) throws MakeDirectoryException;

    void removeDirectory(CloudDirectory directory) throws RemoveDirectoryException;

    void makeRoot(CloudDirectory root, FuseConfiguration fuseConfiguration) throws MakeRootException;

    void synchronizeChildrenPaths(CloudDirectory directory) throws SynchronizeChildrenException;

    void synchronizeFileSize(CloudFile file) throws FileSizeRequestException;

    boolean isReady();
}
