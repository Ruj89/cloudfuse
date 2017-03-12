package net.ruj.cloudfuse.fuse.filesystem.test;

import net.ruj.cloudfuse.fuse.filesystem.CloudFile;

import java.nio.file.Path;
import java.nio.file.Paths;

public class CloudFileUtils {
    public static CloudFile generateFakeFile(String name) {
        Path path = Paths.get(name);
        return new CloudFile(path, name, null);
    }
}
