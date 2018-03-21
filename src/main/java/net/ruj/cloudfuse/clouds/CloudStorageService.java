package net.ruj.cloudfuse.clouds;

import net.ruj.cloudfuse.clouds.exceptions.*;
import net.ruj.cloudfuse.fuse.FuseConfiguration;
import net.ruj.cloudfuse.fuse.filesystem.VirtualDirectory;
import net.ruj.cloudfuse.fuse.filesystem.VirtualFS;
import net.ruj.cloudfuse.fuse.filesystem.VirtualFile;

import java.nio.file.Path;

public interface CloudStorageService {
    void init(Path mountPoint, VirtualFS virtualFS) throws MakeRootException;

    void createFile(VirtualDirectory parent, VirtualFile file) throws CreateFileException;

    void uploadFile(VirtualFile file, byte[] bytesRead, long writeOffset, byte[] bytesToWrite) throws UploadFileException;

    int downloadFile(VirtualFile file, byte[] bytesRead, long offset, int bytesToRead) throws DownloadFileException;

    void truncateFile(VirtualFile file, long size) throws TruncateFileException;

    void removeFile(VirtualFile file) throws RemoveFileException;

    void makeDirectory(VirtualDirectory parent, VirtualDirectory directory) throws MakeDirectoryException;

    void removeDirectory(VirtualDirectory directory) throws RemoveDirectoryException;

    void makeRoot(VirtualDirectory root, FuseConfiguration fuseConfiguration) throws MakeRootException;

    void synchronizeChildrenPaths(VirtualDirectory directory) throws SynchronizeChildrenException;

    void synchronizeFileSize(VirtualFile file) throws FileSizeRequestException;

    boolean isReady();
}
