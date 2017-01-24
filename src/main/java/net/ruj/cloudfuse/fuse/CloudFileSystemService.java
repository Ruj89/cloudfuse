package net.ruj.cloudfuse.fuse;

import jnr.ffi.provider.ClosureManager;
import jnr.ffi.provider.jffi.NativeRuntime;
import net.ruj.cloudfuse.clouds.CloudPathInfo;
import net.ruj.cloudfuse.clouds.CloudStorageService;
import net.ruj.cloudfuse.clouds.exceptions.*;
import net.ruj.cloudfuse.fuse.eventhandlers.DirectoryEventHandler;
import net.ruj.cloudfuse.fuse.eventhandlers.FileEventHandler;
import net.ruj.cloudfuse.fuse.filesystem.CloudDirectory;
import net.ruj.cloudfuse.fuse.filesystem.CloudFS;
import net.ruj.cloudfuse.fuse.filesystem.CloudFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.file.Paths;

import static org.springframework.util.ReflectionUtils.findField;

@Service
public class CloudFileSystemService implements DirectoryEventHandler, FileEventHandler {
    private static final Logger logger = LoggerFactory.getLogger(CloudFileSystemService.class);
    private final FuseConfiguration fuseConfiguration;
    private CloudFS cloudFS;
    private CloudStorageService cloudStorageService;

    @Autowired
    public CloudFileSystemService(FuseConfiguration fuseConfiguration) {
        this.fuseConfiguration = fuseConfiguration;
    }

    public void init(CloudStorageService cloudStorageService) throws IllegalAccessException, MakeRootException {
        this.cloudStorageService = cloudStorageService;
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
    public void onDirectoryAdded(CloudDirectory parent, CloudDirectory directory) throws MakeDirectoryException {
        cloudStorageService.makeDirectory(parent, directory);
    }

    @Override
    public void onDirectoryRemoved(CloudDirectory directory) throws RemoveDirectoryException {
        cloudStorageService.removeDirectory(directory);
        logger.info("Directory removed");
    }

    @Override
    public void onDirectorySynchronized(CloudDirectory directory, CloudPathInfo cloudPathInfo) {
        logger.info("Directory synchronized");
        directory.addEventHandler(this);
        directory.setCloudPathInfo(cloudPathInfo);
        directory.synchronizeChildrenPaths();
    }

    @Override
    public void onFileAdded(CloudDirectory parent, CloudFile file) throws CreateFileException {
        cloudStorageService.createFile(parent, file);
        logger.info("File added in parent");
        file.addEventHandler(this);
    }

    @Override
    public void onFileSynchronized(CloudFile file, CloudPathInfo cloudPathInfo) {
        logger.info("File synchronized");
        file.addEventHandler(this);
        file.setCloudPathInfo(cloudPathInfo);
    }

    @Override
    public void onFileChanged(CloudFile file) throws UploadFileException {
        cloudStorageService.uploadFile(file);
        logger.info("File modified");
    }

    @Override
    public void onFileRemoved(CloudFile file) throws RemoveFileException {
        cloudStorageService.removeFile(file);
        logger.info("File removed");
    }

    public void onRootMounted(CloudDirectory root) throws MakeRootException {
        cloudStorageService.makeRoot(root, fuseConfiguration);
    }

    @Override
    public void synchronizeChildrenPaths(CloudDirectory directory) throws SynchronizeChildrenException {
        cloudStorageService.synchronizeChildrenPaths(directory);
    }

    @Override
    public long fileSize(CloudFile file) throws FileSizeRequestException {
        return file.getCloudPathInfo().getFileSize();
    }

    @Override
    public InputStream fileInputStream(CloudFile file) throws DownloadFileException {
        InputStream is = cloudStorageService.downloadFile(file);
        logger.info("File downloaded");
        return is;
    }
}
