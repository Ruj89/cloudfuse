package net.ruj.cloudfuse.fuse.filesystem;

import net.ruj.cloudfuse.clouds.VirtualPathInfo;
import net.ruj.cloudfuse.clouds.gdrive.GDriveVirtualPathInfo;
import net.ruj.cloudfuse.fuse.exceptions.CloudPathInfoNotFound;
import ru.serce.jnrfuse.struct.FileStat;

import java.nio.file.Path;

public abstract class VirtualPath {
    String name;
    VirtualDirectory parent;
    Path path;
    VirtualPathInfo virtualPathInfo;

    VirtualPath(Path path, String name) {
        this(path, name, null);
    }

    VirtualPath(Path path, String name, VirtualDirectory parent) {
        this.path = path;
        this.name = name;
        this.parent = parent;
    }

    synchronized void delete() {
        if (parent != null) {
            parent.deleteChild(this);
            parent = null;
        }
        remove();
    }

    abstract void remove();

    protected VirtualPath find(String path) {
        while (path.startsWith("/")) {
            path = path.substring(1);
        }
        if (path.equals(name) || path.isEmpty()) {
            return this;
        }
        return null;
    }

    protected abstract void getattr(FileStat stat);

    void rename(String newName) {
        while (newName.startsWith("/")) {
            newName = newName.substring(1);
        }
        name = newName;
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public VirtualPathInfo getVirtualPathInfo() {
        return virtualPathInfo;
    }

    public void setVirtualPathInfo(VirtualPathInfo virtualPathInfo) {
        this.virtualPathInfo = virtualPathInfo;
    }

    public String extractPathId() throws CloudPathInfoNotFound {
        if (virtualPathInfo instanceof GDriveVirtualPathInfo)
            return ((GDriveVirtualPathInfo) getVirtualPathInfo()).getLinkedFileInfo().getId();
        throw new CloudPathInfoNotFound();
    }
}
