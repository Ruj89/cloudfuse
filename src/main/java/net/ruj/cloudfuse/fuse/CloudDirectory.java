package net.ruj.cloudfuse.fuse;

import jnr.ffi.Pointer;
import ru.serce.jnrfuse.FuseFillDir;
import ru.serce.jnrfuse.struct.FileStat;

import java.util.ArrayList;
import java.util.List;

public class CloudDirectory extends CloudPath {
    private List<CloudPath> contents = new ArrayList<>();

    CloudDirectory(String name) {
        super(name);
    }

    private CloudDirectory(String name, CloudDirectory parent) {
        super(name, parent);
    }

    synchronized void add(CloudPath p) {
        contents.add(p);
        p.parent = this;
    }

    synchronized void deleteChild(CloudPath child) {
        contents.remove(child);
    }

    @Override
    protected CloudPath find(String path) {
        if (super.find(path) != null) {
            return super.find(path);
        }
        while (path.startsWith("/")) {
            path = path.substring(1);
        }
        synchronized (this) {
            if (!path.contains("/")) {
                for (CloudPath p : contents) {
                    if (p.name.equals(path)) {
                        return p;
                    }
                }
                return null;
            }
            String nextName = path.substring(0, path.indexOf("/"));
            String rest = path.substring(path.indexOf("/"));
            for (CloudPath p : contents) {
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
        contents.add(new CloudDirectory(lastComponent, this));
    }

    synchronized void mkfile(String lastComponent) {
        contents.add(new CloudFile(lastComponent, this));
    }

    synchronized void read(Pointer buf, FuseFillDir filler) {
        for (CloudPath p : contents) {
            filler.apply(buf, p.name, null, 0);
        }
    }
}
