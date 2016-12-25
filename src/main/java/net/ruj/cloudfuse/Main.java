package net.ruj.cloudfuse;

import net.ruj.cloudfuse.fuse.MemoryFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;

public class Main {
    static Logger logger = LoggerFactory.getLogger(Main.class);
    public static void main(String[] args) {
        MemoryFS memfs = new MemoryFS();
        try {
            logger.info("Starting application");
            memfs.mount(Paths.get("/tmp/mnt"), true);
        } finally {
            memfs.umount();
        }
    }
}
