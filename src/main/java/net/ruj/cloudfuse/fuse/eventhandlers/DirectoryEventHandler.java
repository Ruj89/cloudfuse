package net.ruj.cloudfuse.fuse.eventhandlers;

import net.ruj.cloudfuse.clouds.CloudPathInfo;
import net.ruj.cloudfuse.clouds.exceptions.CreateFileException;
import net.ruj.cloudfuse.clouds.exceptions.MakeDirectoryException;
import net.ruj.cloudfuse.clouds.exceptions.RemoveDirectoryException;
import net.ruj.cloudfuse.clouds.exceptions.SynchronizeChildrenException;
import net.ruj.cloudfuse.fuse.filesystem.VirtualDirectory;
import net.ruj.cloudfuse.fuse.filesystem.CloudFile;

public interface DirectoryEventHandler {
    void onDirectoryAdded(VirtualDirectory parent, VirtualDirectory directory) throws MakeDirectoryException;

    void onDirectoryRemoved(VirtualDirectory virtualDirectory) throws RemoveDirectoryException;

    void onDirectorySynchronized(VirtualDirectory directory, CloudPathInfo cloudPathInfo);

    void onFileAdded(VirtualDirectory parent, CloudFile file) throws CreateFileException;

    void onFileSynchronized(CloudFile file, CloudPathInfo cloudPathInfo);

    void synchronizeChildrenPaths(VirtualDirectory directory) throws SynchronizeChildrenException;
}
