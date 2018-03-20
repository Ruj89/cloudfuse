package net.ruj.cloudfuse.fuse;

import jnr.ffi.provider.ClosureManager;
import jnr.ffi.provider.jffi.NativeRuntime;
import net.ruj.cloudfuse.clouds.CloudPathInfo;
import net.ruj.cloudfuse.clouds.CloudStorageService;
import net.ruj.cloudfuse.clouds.exceptions.*;
import net.ruj.cloudfuse.fuse.eventhandlers.DirectoryEventHandler;
import net.ruj.cloudfuse.fuse.eventhandlers.FileEventHandler;
import net.ruj.cloudfuse.fuse.exceptions.CloudStorageServiceNotFound;
import net.ruj.cloudfuse.fuse.filesystem.VirtualDirectory;
import net.ruj.cloudfuse.fuse.filesystem.CloudFS;
import net.ruj.cloudfuse.fuse.filesystem.CloudFile;
import net.ruj.cloudfuse.services.AggregatorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.lang.reflect.Field;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;

import static org.springframework.util.ReflectionUtils.findField;

@Service
public class CloudFileSystemService implements DirectoryEventHandler, FileEventHandler {
    private static final Logger logger = LoggerFactory.getLogger(CloudFileSystemService.class);

    private final FuseConfiguration fuseConfiguration;
    private final AggregatorService aggregatorService;

    private CloudFS cloudFS;
    //TODO: handle multiple cloud storages
    private ArrayList<CloudStorageService> cloudStorageServices = new ArrayList<>();
    private boolean alreadyInitialized = false;

    @Autowired
    public CloudFileSystemService(
            FuseConfiguration fuseConfiguration,
            AggregatorService aggregatorService
    ) {
        this.fuseConfiguration = fuseConfiguration;
        this.aggregatorService = aggregatorService;
    }

    public void init() throws MakeRootException, IllegalAccessException {
        if (alreadyInitialized || cloudStorageServices.size() == 0)
            return;
        alreadyInitialized = true;
        initLibFuse();
        cloudStorageServices.stream()
                .filter(CloudStorageService::isReady)
                .forEach(cloudStorageService -> {
                    try {
                        cloudFS = new CloudFS(this);
                        cloudStorageService.init(
                                Paths.get(fuseConfiguration.getDrive().getLocalFolder()),
                                cloudFS
                        );
                    } catch (MakeRootException e) {
                        e.printStackTrace();
                    }
                });
    }

    private void initLibFuse() throws IllegalAccessException {
        ClosureManager closureManager = NativeRuntime.getInstance().getClosureManager();
        Field classLoader = findField(closureManager.getClass(), "classLoader");
        classLoader.setAccessible(true);
        Object asmClassLoader = classLoader.get(closureManager);

        Field parent = findField(asmClassLoader.getClass(), "parent");
        parent.setAccessible(true);
        parent.set(asmClassLoader, Thread.currentThread().getContextClassLoader());
    }

    @SuppressWarnings("unused")
    @PreDestroy
    private void destroy() {
        cloudFS.umount();
    }

    @Override
    public void onDirectoryAdded(VirtualDirectory parent, VirtualDirectory directory) throws MakeDirectoryException {
        Optional<MakeDirectoryException> eO = cloudStorageServices.stream()
                .map(css -> {
                    try {
                        css.makeDirectory(parent, directory);
                        return null;
                    } catch (MakeDirectoryException e) {
                        return e;
                    }
                })
                .filter(Objects::nonNull)
                .findAny();
        if (eO.isPresent())
            throw eO.get();
    }

    @Override
    public void onDirectoryRemoved(VirtualDirectory directory) throws RemoveDirectoryException {
        try {
            cloudStorageServices.stream()
                    .findAny()
                    .orElseThrow(CloudStorageServiceNotFound::new)
                    .removeDirectory(directory);
            logger.info("Directory removed");
        } catch (Exception e) {
            throw new RemoveDirectoryException(e);
        }
    }

    @Override
    public void onDirectorySynchronized(VirtualDirectory directory, CloudPathInfo cloudPathInfo) {
        logger.info("Directory synchronized");
        directory.addEventHandler(this);
        directory.setCloudPathInfo(cloudPathInfo);
        directory.synchronizeChildrenPaths();
    }

    @Override
    public void onFileAdded(VirtualDirectory parent, CloudFile file) throws CreateFileException {
        try {
            cloudStorageServices.stream()
                    .findAny()
                    .orElseThrow(CloudStorageServiceNotFound::new)
                    .createFile(parent, file);
            logger.info("File added in parent");
            file.addEventHandler(this);
        } catch (Exception e) {
            throw new CreateFileException(e);
        }
    }

    @Override
    public void onFileSynchronized(CloudFile file, CloudPathInfo cloudPathInfo) {
        logger.info("File synchronized");
        file.addEventHandler(this);
        file.setCloudPathInfo(cloudPathInfo);
    }

    @Override
    public void onFileChanged(CloudFile file, long writeOffset, byte[] bytesToWrite) throws UploadFileException {
        try {
            aggregatorService.changeFile(
                    cloudStorageServices.stream()
                            .findAny()
                            .orElseThrow(CloudStorageServiceNotFound::new),
                    file,
                    writeOffset,
                    bytesToWrite
            );
            logger.info("File modified");
        } catch (Exception e) {
            throw new UploadFileException(e);
        }
    }

    @Override
    public void onFileRemoved(CloudFile file) throws RemoveFileException {
        try {
            cloudStorageServices.stream()
                    .findAny()
                    .orElseThrow(CloudStorageServiceNotFound::new)
                    .removeFile(file);
            logger.info("File removed");
        } catch (Exception e) {
            throw new RemoveFileException(e);
        }
    }

    public void onRootMounted(VirtualDirectory root) throws MakeRootException {
        try {
            cloudStorageServices.stream()
                    .findAny()
                    .orElseThrow(CloudStorageServiceNotFound::new)
                    .makeRoot(root, fuseConfiguration);
        } catch (Exception e) {
            throw new MakeRootException(e);
        }
    }

    @Override
    public void synchronizeChildrenPaths(VirtualDirectory directory) throws SynchronizeChildrenException {
        try {
            cloudStorageServices.stream()
                    .findAny()
                    .orElseThrow(CloudStorageServiceNotFound::new)
                    .synchronizeChildrenPaths(directory);
        } catch (Exception e) {
            throw new SynchronizeChildrenException(e);
        }
    }

    @Override
    public long fileSize(CloudFile file) throws FileSizeRequestException {
        try {
            return file.getCloudPathInfo().getFileSize();
        } catch (Exception e) {
            throw new FileSizeRequestException(e);
        }
    }

    @Override
    public void onFileTruncated(CloudFile file, long size) throws TruncateFileException {
        try {
            cloudStorageServices.stream()
                    .findAny()
                    .orElseThrow(CloudStorageServiceNotFound::new)
                    .truncateFile(file, size);
            logger.info("File truncated");
        } catch (Exception e) {
            throw new TruncateFileException(e);
        }
    }

    @Override
    public int onFileRead(CloudFile file, byte[] bytesRead, long offset, int bytesToRead) throws DownloadFileException {
        try {
            int resultBytes = aggregatorService.downloadFile(
                    cloudStorageServices.stream()
                            .findAny()
                            .orElseThrow(CloudStorageServiceNotFound::new),
                    file,
                    bytesRead,
                    offset,
                    bytesToRead
            );
            logger.info("File downloaded");
            return resultBytes;
        } catch (Exception e) {
            throw new DownloadFileException(e);
        }
    }

    public void addCloudStorageService(CloudStorageService service)
            throws MakeRootException, IllegalAccessException {
        this.cloudStorageServices.add(service);
        if (fuseConfiguration.isAutomount()) {
            init();
        }
    }
}
