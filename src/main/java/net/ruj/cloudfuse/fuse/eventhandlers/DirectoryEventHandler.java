package net.ruj.cloudfuse.fuse.eventhandlers;

import net.ruj.cloudfuse.clouds.VirtualPathInfo;
import net.ruj.cloudfuse.clouds.exceptions.CreateFileException;
import net.ruj.cloudfuse.clouds.exceptions.MakeDirectoryException;
import net.ruj.cloudfuse.clouds.exceptions.RemoveDirectoryException;
import net.ruj.cloudfuse.clouds.exceptions.SynchronizeChildrenException;
import net.ruj.cloudfuse.fuse.filesystem.VirtualDirectory;
import net.ruj.cloudfuse.fuse.filesystem.VirtualFile;

public interface DirectoryEventHandler {
    void onDirectoryAdded(VirtualDirectory parent, VirtualDirectory directory) throws MakeDirectoryException;

    void onDirectoryRemoved(VirtualDirectory virtualDirectory) throws RemoveDirectoryException;

    void onDirectorySynchronized(VirtualDirectory directory, VirtualPathInfo virtualPathInfo);

    void onFileAdded(VirtualDirectory parent, VirtualFile file) throws CreateFileException;

    void onFileSynchronized(VirtualFile file, VirtualPathInfo virtualPathInfo);

    void synchronizeChildrenPaths(VirtualDirectory directory) throws SynchronizeChildrenException;
}
