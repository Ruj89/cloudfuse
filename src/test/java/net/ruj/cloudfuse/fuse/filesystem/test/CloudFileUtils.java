package net.ruj.cloudfuse.fuse.filesystem.test;

import net.ruj.cloudfuse.fuse.filesystem.VirtualFile;

import java.nio.file.Path;
import java.nio.file.Paths;

public class CloudFileUtils {
    public static VirtualFile generateFakeFile(String name) {
        Path path = Paths.get(name);
        return new VirtualFile(path, name, null);
    }
}
