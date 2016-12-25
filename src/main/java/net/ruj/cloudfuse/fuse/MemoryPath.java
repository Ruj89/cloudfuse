package net.ruj.cloudfuse.fuse;

import ru.serce.jnrfuse.struct.FileStat;

public abstract class MemoryPath {
    String name;
    MemoryDirectory parent;

    MemoryPath(String name) {
        this(name, null);
    }

    MemoryPath(String name, MemoryDirectory parent) {
        this.name = name;
        this.parent = parent;
    }

    synchronized void delete() {
        if (parent != null) {
            parent.deleteChild(this);
            parent = null;
        }
    }

    protected MemoryPath find(String path) {
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
