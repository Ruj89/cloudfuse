package net.ruj.cloudfuse.fuse;

import net.ruj.cloudfuse.fuse.configuration.FuseConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.nio.file.Paths;

@Service
@Profile("linux")
public class CloudFileSystemService {
    static final Logger logger = LoggerFactory.getLogger(CloudFileSystemService.class);
    private final CloudFS cloudFS;
    private final FuseConfiguration fuseConfiguration;

    @Autowired
    public CloudFileSystemService(CloudFS cloudFS, FuseConfiguration fuseConfiguration) {
        this.cloudFS = cloudFS;
        this.fuseConfiguration = fuseConfiguration;
    }

    @PostConstruct
    private void init() {
        String localDriveFolder = fuseConfiguration.getDrive().getLocalFolder();
        logger.info("Mounting Google Drive fuse partition on '" + localDriveFolder + "'...");
        try {
            cloudFS.mount(Paths.get(localDriveFolder), true);
        } finally {
            cloudFS.umount();
        }
    }
}
