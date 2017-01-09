package net.ruj.cloudfuse.fuse;

import ru.serce.jnrfuse.struct.FileStat;

public abstract class CloudPath {
    String name;
    CloudDirectory parent;

    CloudPath(String name) {
        this(name, null);
    }

    CloudPath(String name, CloudDirectory parent) {
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
}
