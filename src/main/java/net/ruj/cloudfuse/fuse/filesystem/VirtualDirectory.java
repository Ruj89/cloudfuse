package net.ruj.cloudfuse.fuse.filesystem;

import jnr.ffi.Pointer;
import net.ruj.cloudfuse.clouds.VirtualPathInfo;
import net.ruj.cloudfuse.clouds.exceptions.CreateFileException;
import net.ruj.cloudfuse.clouds.exceptions.MakeDirectoryException;
import net.ruj.cloudfuse.clouds.exceptions.RemoveDirectoryException;
import net.ruj.cloudfuse.clouds.exceptions.SynchronizeChildrenException;
import net.ruj.cloudfuse.fuse.eventhandlers.DirectoryEventHandler;
import net.ruj.cloudfuse.fuse.eventhandlers.FileEventHandler;
import ru.serce.jnrfuse.FuseFillDir;
import ru.serce.jnrfuse.struct.FileStat;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class VirtualDirectory extends VirtualPath {
    private List<VirtualPath> contents = new ArrayList<>();
    private Set<DirectoryEventHandler> directoryEventHandlers = new HashSet<>();
    private Set<FileEventHandler> childrenFilesEventHandlers = new HashSet<>();

    VirtualDirectory(Path path, String name) {
        super(path, name);
    }

    private VirtualDirectory(Path path, String name, VirtualDirectory parent) {
        super(path, name, parent);
    }

    synchronized void add(VirtualPath p) {
        contents.add(p);
        p.parent = this;
    }

    synchronized void deleteChild(VirtualPath child) {
        contents.remove(child);
    }

    @Override
    protected VirtualPath find(String path) {
        if (super.find(path) != null) {
            return super.find(path);
        }
        while (path.startsWith("/")) {
            path = path.substring(1);
        }
        synchronized (this) {
            if (!path.contains("/")) {
                for (VirtualPath p : contents) {
                    if (p.name.equals(path)) {
                        return p;
                    }
                }
                return null;
            }
            String nextName = path.substring(0, path.indexOf("/"));
            String rest = path.substring(path.indexOf("/"));
            for (VirtualPath p : contents) {
                if (p.name.equals(nextName)) {
                    return p.find(rest);
                }
            }
        }
        return null;
    }

    @Override
    protected void getattr(FileStat stat) {
        stat.st_mode.set(FileStat.S_IFDIR);
    }

    synchronized void mkdir(String lastComponent) {
        mkdir(lastComponent, null);
    }

    public synchronized void mkdir(String folderName, VirtualPathInfo virtualPathInfo) {
        VirtualDirectory directory = new VirtualDirectory(Paths.get(path.toString(), folderName), folderName, this);
        contents.add(directory);
        this.directoryEventHandlers.forEach(directory::addEventHandler);
        this.childrenFilesEventHandlers.forEach(directory::addChildrenFileEventHandler);
        if (virtualPathInfo == null)
            directoryEventHandlers.forEach(deh -> {
                try {
                    deh.onDirectoryAdded(this, directory);
                } catch (MakeDirectoryException e) {
                    e.printStackTrace();
                }
            });
        else directoryEventHandlers.forEach(deh -> deh.onDirectorySynchronized(directory, virtualPathInfo));
    }

    synchronized void mkfile(String lastComponent) {
        mkfile(lastComponent, null);
    }

    public synchronized void mkfile(String lastComponent, VirtualPathInfo virtualPathInfo) {
        VirtualFile file = new VirtualFile(Paths.get(path.toString(), lastComponent), lastComponent, this);
        contents.add(file);
        this.childrenFilesEventHandlers.forEach(file::addEventHandler);
        if (virtualPathInfo == null)
            directoryEventHandlers.forEach(deh -> {
                try {
                    deh.onFileAdded(this, file);
                } catch (CreateFileException e) {
                    e.printStackTrace();
                }
            });
        else directoryEventHandlers.forEach(deh -> deh.onFileSynchronized(file, virtualPathInfo));
    }

    synchronized void read(Pointer buf, FuseFillDir filler) {
        contents.forEach(c -> filler.apply(buf, c.name, null, 0));
    }

    @Override
    void remove() {
        directoryEventHandlers.forEach(deh -> {
            try {
                deh.onDirectoryRemoved(this);
            } catch (RemoveDirectoryException e) {
                e.printStackTrace();
            }
        });
    }

    public void synchronizeChildrenPaths() {
        contents.clear();
        directoryEventHandlers.forEach(deh -> {
            try {
                deh.synchronizeChildrenPaths(this);
            } catch (SynchronizeChildrenException e) {
                e.printStackTrace();
            }
        });
    }

    public VirtualDirectory addEventHandler(DirectoryEventHandler eventHandler) {
        directoryEventHandlers.add(eventHandler);
        return this;
    }

    public VirtualDirectory addChildrenFileEventHandler(FileEventHandler eventHandler){
        childrenFilesEventHandlers.add(eventHandler);
        return this;
    }
}
