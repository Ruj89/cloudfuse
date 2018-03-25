package net.ruj.cloudfuse.fuse;

import jnr.ffi.provider.ClosureManager;
import jnr.ffi.provider.jffi.NativeRuntime;
import net.ruj.cloudfuse.CloudFuseConfiguration;
import net.ruj.cloudfuse.clouds.CloudStorageService;
import net.ruj.cloudfuse.clouds.VirtualPathInfo;
import net.ruj.cloudfuse.clouds.exceptions.*;
import net.ruj.cloudfuse.fuse.eventhandlers.DirectoryEventHandler;
import net.ruj.cloudfuse.fuse.eventhandlers.FileEventHandler;
import net.ruj.cloudfuse.fuse.exceptions.CloudStorageServiceNotFound;
import net.ruj.cloudfuse.fuse.filesystem.VirtualDirectory;
import net.ruj.cloudfuse.fuse.filesystem.VirtualFS;
import net.ruj.cloudfuse.fuse.filesystem.VirtualFile;
import net.ruj.cloudfuse.services.AggregatorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;

import static org.springframework.util.ReflectionUtils.findField;

@Service
public class VirtualFileSystemService implements DirectoryEventHandler, FileEventHandler {
    private static final Logger logger = LoggerFactory.getLogger(VirtualFileSystemService.class);

    private final CloudFuseConfiguration cloudFuseConfiguration;
    private final AggregatorService aggregatorService;

    private VirtualFS virtualFS;
    //TODO: handle multiple cloud storages
    private ArrayList<CloudStorageService> cloudStorageServices = new ArrayList<>();
    private boolean alreadyInitialized = false;

    @Autowired
    public VirtualFileSystemService(
            CloudFuseConfiguration cloudFuseConfiguration,
            AggregatorService aggregatorService
    ) {
        this.cloudFuseConfiguration = cloudFuseConfiguration;
        this.aggregatorService = aggregatorService;
    }

    public void init() throws IllegalAccessException {
        if (alreadyInitialized || cloudStorageServices.size() == 0)
            return;
        alreadyInitialized = true;
        initLibFuse();
        cloudStorageServices.stream()
                .filter(CloudStorageService::isReady)
                .forEach(cloudStorageService -> {
                    try {
                        virtualFS = new VirtualFS(this);
                        cloudStorageService.init(virtualFS);
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
        virtualFS.umount();
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
    public void onDirectorySynchronized(VirtualDirectory directory, VirtualPathInfo virtualPathInfo) {
        logger.info("Directory synchronized");
        directory.setVirtualPathInfo(virtualPathInfo);
        directory.synchronizeChildrenPaths();
    }

    @Override
    public void onFileAdded(VirtualDirectory parent, VirtualFile file) throws CreateFileException {
        try {
            cloudStorageServices.stream()
                    .findAny()
                    .orElseThrow(CloudStorageServiceNotFound::new)
                    .createFile(parent, file);
            logger.info("File added in parent");
        } catch (Exception e) {
            throw new CreateFileException(e);
        }
    }

    @Override
    public void onFileSynchronized(VirtualFile file, VirtualPathInfo virtualPathInfo) {
        logger.info("File synchronized");
        file.setVirtualPathInfo(virtualPathInfo);
    }

    @Override
    public void onFileChanged(VirtualFile file, long writeOffset, byte[] bytesToWrite) throws UploadFileException {
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
    public void onFileRemoved(VirtualFile file) throws RemoveFileException {
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

    public void onRootDirectoryInit(VirtualDirectory root) throws MakeRootException {
        try {
            cloudStorageServices.stream()
                    .findAny()
                    .orElseThrow(CloudStorageServiceNotFound::new)
                    .makeRoot(root, cloudFuseConfiguration);
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
    public long fileSize(VirtualFile file) throws FileSizeRequestException {
        try {
            return file.getVirtualPathInfo().getFileSize();
        } catch (Exception e) {
            throw new FileSizeRequestException(e);
        }
    }

    @Override
    public void onFileTruncated(VirtualFile file, long size) throws TruncateFileException {
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
    public int onFileRead(VirtualFile file, byte[] bytesRead, long offset, int bytesToRead) throws DownloadFileException {
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
            throws IllegalAccessException {
        this.cloudStorageServices.add(service);
        if (cloudFuseConfiguration.isAutomount())
            init();
    }
}
