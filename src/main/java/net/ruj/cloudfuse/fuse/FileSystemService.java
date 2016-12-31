package net.ruj.cloudfuse.fuse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.nio.file.Paths;

@Service
@Profile("linux")
public class FileSystemService {
    static final Logger logger = LoggerFactory.getLogger(FileSystemService.class);
    private final MemoryFS memoryFS;

    @Autowired
    public FileSystemService(MemoryFS memoryFS) {
        this.memoryFS = memoryFS;
    }

    @PostConstruct
    private void init() {
        logger.info("Mounting fuse partition...");
        try {
            memoryFS.mount(Paths.get("/tmp/mnt"), true);
        } finally {
            memoryFS.umount();
        }
    }
}
