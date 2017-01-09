package net.ruj.cloudfuse.fuse;

import jnr.ffi.provider.ClosureManager;
import jnr.ffi.provider.jffi.NativeRuntime;
import net.ruj.cloudfuse.fuse.configuration.FuseConfiguration;
import net.ruj.cloudfuse.gdrive.GDriveService;
import net.ruj.cloudfuse.notifications.eventhandlers.DirectoryEventHandler;
import net.ruj.cloudfuse.notifications.eventhandlers.FileEventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.lang.reflect.Field;
import java.nio.file.Paths;

import static org.springframework.util.ReflectionUtils.findField;

@Service
@Profile("linux")
public class CloudFileSystemService implements DirectoryEventHandler, FileEventHandler {
    private static final Logger logger = LoggerFactory.getLogger(CloudFileSystemService.class);
    private final FuseConfiguration fuseConfiguration;
    private final GDriveService gDriveService;
    private CloudFS cloudFS;

    @Autowired
    public CloudFileSystemService(FuseConfiguration fuseConfiguration, GDriveService gDriveService) {
        this.fuseConfiguration = fuseConfiguration;
        this.gDriveService = gDriveService;
    }

    @PostConstruct
    private void init() throws IllegalAccessException {
        ClosureManager closureManager = NativeRuntime.getInstance().getClosureManager();
        Field classLoader = findField(closureManager.getClass(), "classLoader");
        classLoader.setAccessible(true);
        Object asmClassLoader = classLoader.get(closureManager);

        Field parent = findField(asmClassLoader.getClass(), "parent");
        parent.setAccessible(true);
        parent.set(asmClassLoader, Thread.currentThread().getContextClassLoader());

        cloudFS = new CloudFS(this);
        String localDriveFolder = fuseConfiguration.getDrive().getLocalFolder();
        logger.info("Mounting Google Drive fuse partition on '" + localDriveFolder + "'...");
        cloudFS.mount(Paths.get(localDriveFolder), false);
        logger.info("Google Drive mounted!");
    }

    @PreDestroy
    private void destroy() {
        cloudFS.umount();
    }

    @Override
    public void fileAdded(CloudDirectory directory, CloudFile file) {
        logger.info("File added in directory");
        file.addEventHandler(this);
    }

    @Override
    public void fileChanged(CloudFile cloudFile) {
        gDriveService.uploadFile(cloudFile.getPath(), cloudFile.getContents());
        logger.info("File modified");
    }
}
