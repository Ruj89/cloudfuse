package net.ruj.cloudfuse.fuse.filesystem;

import net.ruj.cloudfuse.clouds.CloudPathInfo;
import ru.serce.jnrfuse.struct.FileStat;

import java.nio.file.Path;

public abstract class CloudPath {
    String name;
    CloudDirectory parent;
    Path path;
    CloudPathInfo cloudPathInfo;

    CloudPath(Path path, String name) {
        this(path, name, null);
    }

    CloudPath(Path path, String name, CloudDirectory parent) {
        this.path = path;
        this.name = name;
        this.parent = parent;
    }

    synchronized void delete() {
        if (parent != null) {
            parent.deleteChild(this);
            parent = null;
        }
    }

    protected CloudPath find(String path) {
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

    public CloudPathInfo getCloudPathInfo() {
        return cloudPathInfo;
    }

    public void setCloudPathInfo(CloudPathInfo cloudPathInfo) {
        this.cloudPathInfo = cloudPathInfo;
    }
}
