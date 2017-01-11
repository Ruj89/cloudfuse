package net.ruj.cloudfuse.fuse;

import jnr.ffi.Pointer;
import net.ruj.cloudfuse.notifications.eventhandlers.DirectoryEventHandler;
import ru.serce.jnrfuse.FuseFillDir;
import ru.serce.jnrfuse.struct.FileStat;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CloudDirectory extends CloudPath {
    private List<CloudPath> contents = new ArrayList<>();
    private Set<DirectoryEventHandler> directoryEventHandlers = new HashSet<>();

    CloudDirectory(Path path, String name) {
        super(path, name);
    }

    private CloudDirectory(Path path, String name, CloudDirectory parent) {
        super(path, name, parent);
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
        CloudDirectory directory = new CloudDirectory(Paths.get(path.toString(), lastComponent), lastComponent, this);
        contents.add(directory);
        this.directoryEventHandlers.forEach(directory::addEventHandler);
        directoryEventHandlers.forEach(deh -> deh.directoryAdded(this, directory));
    }

    synchronized void mkfile(String lastComponent) {
        CloudFile file = new CloudFile(Paths.get(path.toString(), lastComponent), lastComponent, this);
        contents.add(file);
        directoryEventHandlers.forEach(deh -> deh.fileAdded(this, file));
    }

    synchronized void read(Pointer buf, FuseFillDir filler) {
        contents.forEach(c -> filler.apply(buf, c.name, null, 0));
    }

    public CloudDirectory addEventHandler(DirectoryEventHandler eventHandler) {
        directoryEventHandlers.add(eventHandler);
        return this;
    }
}
