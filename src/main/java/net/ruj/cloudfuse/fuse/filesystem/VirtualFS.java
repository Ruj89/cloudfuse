package net.ruj.cloudfuse.fuse.filesystem;

import jnr.ffi.Pointer;
import jnr.ffi.types.mode_t;
import jnr.ffi.types.off_t;
import jnr.ffi.types.size_t;
import net.ruj.cloudfuse.clouds.exceptions.MakeRootException;
import net.ruj.cloudfuse.fuse.VirtualFileSystemService;
import ru.serce.jnrfuse.ErrorCodes;
import ru.serce.jnrfuse.FuseFillDir;
import ru.serce.jnrfuse.FuseStubFS;
import ru.serce.jnrfuse.struct.FileStat;
import ru.serce.jnrfuse.struct.FuseFileInfo;

import java.nio.file.Paths;

public class VirtualFS extends FuseStubFS {
    private VirtualDirectory rootDirectory;

    public VirtualFS(VirtualFileSystemService virtualFileSystemService) throws MakeRootException {
        rootDirectory = new VirtualDirectory(Paths.get("/"), "");
        rootDirectory.addEventHandler(virtualFileSystemService);
        rootDirectory.addChildrenFileEventHandler(virtualFileSystemService);
        virtualFileSystemService.onRootDirectoryInit(rootDirectory);
    }

    @Override
    public int create(String path, @mode_t long mode, FuseFileInfo fi) {
        if (getPath(path) != null) {
            return -ErrorCodes.EEXIST();
        }
        VirtualPath parent = getParentPath(path);
        if (parent instanceof VirtualDirectory) {
            ((VirtualDirectory) parent).mkfile(getLastComponent(path));
            return 0;
        }
        return -ErrorCodes.ENOENT();
    }


    @Override
    public int getattr(String path, FileStat stat) {
        VirtualPath p = getPath(path);
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

    private VirtualPath getParentPath(String path) {
        return rootDirectory.find(path.substring(0, path.lastIndexOf("/")));
    }

    private VirtualPath getPath(String path) {
        return rootDirectory.find(path);
    }

    @Override
    public int mkdir(String path, @mode_t long mode) {
        if (getPath(path) != null) {
            return -ErrorCodes.EEXIST();
        }
        VirtualPath parent = getParentPath(path);
        if (parent instanceof VirtualDirectory) {
            ((VirtualDirectory) parent).mkdir(getLastComponent(path));
            return 0;
        }
        return -ErrorCodes.ENOENT();
    }

    @Override
    public int read(String path, Pointer buf, @size_t long size, @off_t long offset, FuseFileInfo fi) {
        VirtualPath p = getPath(path);
        if (p == null) {
            return -ErrorCodes.ENOENT();
        }
        if (!(p instanceof VirtualFile)) {
            return -ErrorCodes.EISDIR();
        }
        return ((VirtualFile) p).read(buf, size, offset);
    }

    @Override
    public int readdir(String path, Pointer buf, FuseFillDir filter, @off_t long offset, FuseFileInfo fi) {
        VirtualPath p = getPath(path);
        if (p == null) {
            return -ErrorCodes.ENOENT();
        }
        if (!(p instanceof VirtualDirectory)) {
            return -ErrorCodes.ENOTDIR();
        }
        filter.apply(buf, ".", null, 0);
        filter.apply(buf, "..", null, 0);
        ((VirtualDirectory) p).read(buf, filter);
        return 0;
    }

    @Override
    public int rename(String path, String newName) {
        VirtualPath p = getPath(path);
        if (p == null) {
            return -ErrorCodes.ENOENT();
        }
        VirtualPath newParent = getParentPath(newName);
        if (newParent == null) {
            return -ErrorCodes.ENOENT();
        }
        if (!(newParent instanceof VirtualDirectory)) {
            return -ErrorCodes.ENOTDIR();
        }
        p.delete();
        p.rename(newName.substring(newName.lastIndexOf("/")));
        ((VirtualDirectory) newParent).add(p);
        return 0;
    }

    @Override
    public int rmdir(String path) {
        VirtualPath p = getPath(path);
        if (p == null) {
            return -ErrorCodes.ENOENT();
        }
        if (!(p instanceof VirtualDirectory)) {
            return -ErrorCodes.ENOTDIR();
        }
        p.delete();
        return 0;
    }

    @Override
    public int truncate(String path, long offset) {
        VirtualPath p = getPath(path);
        if (p == null) {
            return -ErrorCodes.ENOENT();
        }
        if (!(p instanceof VirtualFile)) {
            return -ErrorCodes.EISDIR();
        }
        ((VirtualFile) p).truncate(offset);
        return 0;
    }

    @Override
    public int unlink(String path) {
        VirtualPath p = getPath(path);
        if (p == null) {
            return -ErrorCodes.ENOENT();
        }
        p.delete();
        return 0;
    }

    @Override
    public int write(String path, Pointer buf, @size_t long size, @off_t long offset, FuseFileInfo fi) {
        VirtualPath p = getPath(path);
        if (p == null) {
            return -ErrorCodes.ENOENT();
        }
        if (!(p instanceof VirtualFile)) {
            return -ErrorCodes.EISDIR();
        }
        return ((VirtualFile) p).write(buf, Math.toIntExact(size), offset);
    }
}