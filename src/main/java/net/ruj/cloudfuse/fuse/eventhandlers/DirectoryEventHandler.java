package net.ruj.cloudfuse.fuse.eventhandlers;

import net.ruj.cloudfuse.clouds.CloudPathInfo;
import net.ruj.cloudfuse.clouds.exceptions.CreateFileException;
import net.ruj.cloudfuse.clouds.exceptions.MakeDirectoryException;
import net.ruj.cloudfuse.clouds.exceptions.RemoveDirectoryException;
import net.ruj.cloudfuse.clouds.exceptions.SynchronizeChildrenException;
import net.ruj.cloudfuse.fuse.filesystem.CloudDirectory;
import net.ruj.cloudfuse.fuse.filesystem.CloudFile;

public interface DirectoryEventHandler {
    void onDirectoryAdded(CloudDirectory parent, CloudDirectory directory) throws MakeDirectoryException;

    void onDirectoryRemoved(CloudDirectory cloudDirectory) throws RemoveDirectoryException;

    void onDirectorySynchronized(CloudDirectory directory, CloudPathInfo cloudPathInfo);

    void onFileAdded(CloudDirectory parent, CloudFile file) throws CreateFileException;

    void onFileSynchronized(CloudFile file, CloudPathInfo cloudPathInfo);

    void synchronizeChildrenPaths(CloudDirectory directory) throws SynchronizeChildrenException;
}
