package net.ruj.cloudfuse.fuse;

import jnr.ffi.Pointer;
import jnr.ffi.types.mode_t;
import jnr.ffi.types.off_t;
import jnr.ffi.types.size_t;
import ru.serce.jnrfuse.ErrorCodes;
import ru.serce.jnrfuse.FuseFillDir;
import ru.serce.jnrfuse.FuseStubFS;
import ru.serce.jnrfuse.struct.FileStat;
import ru.serce.jnrfuse.struct.FuseFileInfo;

import java.nio.file.Paths;

public class CloudFS extends FuseStubFS {
    private CloudDirectory rootDirectory;

    public CloudFS(CloudFileSystemService cloudFileSystemService) {
        rootDirectory = new CloudDirectory(Paths.get("/"), "");
        rootDirectory.addEventHandler(cloudFileSystemService);
    }

    @Override
    public int create(String path, @mode_t long mode, FuseFileInfo fi) {
        if (getPath(path) != null) {
            return -ErrorCodes.EEXIST();
        }
        CloudPath parent = getParentPath(path);
        if (parent instanceof CloudDirectory) {
            ((CloudDirectory) parent).mkfile(getLastComponent(path));
            return 0;
        }
        return -ErrorCodes.ENOENT();
    }


    @Override
    public int getattr(String path, FileStat stat) {
        CloudPath p = getPath(path);
        if (p != null) {
            p.getattr(stat);
            return 0;
        }
        return -ErrorCodes.ENOENT();
    }

    private String getLastComponent(String path) {
        // Trim final slashes
        while (path.substring(path.length() - 1).equals("/")) {
            path = path.substring(0, path.length() - 1);
        }
        if (path.isEmpty()) {
            return "";
        }
        // Return path name
        return path.substring(path.lastIndexOf("/") + 1);
    }

    private CloudPath getParentPath(String path) {
        return rootDirectory.find(path.substring(0, path.lastIndexOf("/")));
    }

    private CloudPath getPath(String path) {
        return rootDirectory.find(path);
    }

    @Override
    public int mkdir(String path, @mode_t long mode) {
        if (getPath(path) != null) {
            return -ErrorCodes.EEXIST();
        }
        CloudPath parent = getParentPath(path);
        if (parent instanceof CloudDirectory) {
            ((CloudDirectory) parent).mkdir(getLastComponent(path));
            return 0;
        }
        return -ErrorCodes.ENOENT();
    }

    @Override
    public int read(String path, Pointer buf, @size_t long size, @off_t long offset, FuseFileInfo fi) {
        CloudPath p = getPath(path);
        if (p == null) {
            return -ErrorCodes.ENOENT();
        }
        if (!(p instanceof CloudFile)) {
            return -ErrorCodes.EISDIR();
        }
        return ((CloudFile) p).read(buf, size, offset);
    }

    @Override
    public int readdir(String path, Pointer buf, FuseFillDir filter, @off_t long offset, FuseFileInfo fi) {
        CloudPath p = getPath(path);
        if (p == null) {
            return -ErrorCodes.ENOENT();
        }
        if (!(p instanceof CloudDirectory)) {
            return -ErrorCodes.ENOTDIR();
        }
        filter.apply(buf, ".", null, 0);
        filter.apply(buf, "..", null, 0);
        ((CloudDirectory) p).read(buf, filter);
        return 0;
    }

    @Override
    public int rename(String path, String newName) {
        CloudPath p = getPath(path);
        if (p == null) {
            return -ErrorCodes.ENOENT();
        }
        CloudPath newParent = getParentPath(newName);
        if (newParent == null) {
            return -ErrorCodes.ENOENT();
        }
        if (!(newParent instanceof CloudDirectory)) {
            return -ErrorCodes.ENOTDIR();
        }
        p.delete();
        p.rename(newName.substring(newName.lastIndexOf("/")));
        ((CloudDirectory) newParent).add(p);
        return 0;
    }

    @Override
    public int rmdir(String path) {
        CloudPath p = getPath(path);
        if (p == null) {
            return -ErrorCodes.ENOENT();
        }
        if (!(p instanceof CloudDirectory)) {
            return -ErrorCodes.ENOTDIR();
        }
        p.delete();
        return 0;
    }

    @Override
    public int truncate(String path, long offset) {
        CloudPath p = getPath(path);
        if (p == null) {
            return -ErrorCodes.ENOENT();
        }
        if (!(p instanceof CloudFile)) {
            return -ErrorCodes.EISDIR();
        }
        ((CloudFile) p).truncate(offset);
        return 0;
    }

    @Override
    public int unlink(String path) {
        CloudPath p = getPath(path);
        if (p == null) {
            return -ErrorCodes.ENOENT();
        }
        p.delete();
        return 0;
    }

    @Override
    public int write(String path, Pointer buf, @size_t long size, @off_t long offset, FuseFileInfo fi) {
        CloudPath p = getPath(path);
        if (p == null) {
            return -ErrorCodes.ENOENT();
        }
        if (!(p instanceof CloudFile)) {
            return -ErrorCodes.EISDIR();
        }
        return ((CloudFile) p).write(buf, size, offset);
    }
}