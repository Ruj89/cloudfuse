package net.ruj.cloudfuse.fuse;

import jnr.ffi.provider.ClosureManager;
import jnr.ffi.provider.jffi.NativeRuntime;
import net.ruj.cloudfuse.clouds.CloudPathInfo;
import net.ruj.cloudfuse.clouds.CloudStorageService;
import net.ruj.cloudfuse.clouds.exceptions.*;
import net.ruj.cloudfuse.fuse.eventhandlers.DirectoryEventHandler;
import net.ruj.cloudfuse.fuse.eventhandlers.FileEventHandler;
import net.ruj.cloudfuse.fuse.exceptions.CloudStorageServiceNotFound;
import net.ruj.cloudfuse.fuse.filesystem.CloudDirectory;
import net.ruj.cloudfuse.fuse.filesystem.CloudFS;
import net.ruj.cloudfuse.fuse.filesystem.CloudFile;
import net.ruj.cloudfuse.queues.DownloadQueueItem;
import net.ruj.cloudfuse.queues.DownloadQueueService;
import net.ruj.cloudfuse.queues.UploadQueueItem;
import net.ruj.cloudfuse.queues.UploadQueueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.lang.reflect.Field;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.springframework.util.ReflectionUtils.findField;

@Service
public class CloudFileSystemService implements DirectoryEventHandler, FileEventHandler {
    private static final Logger logger = LoggerFactory.getLogger(CloudFileSystemService.class);

    private final FuseConfiguration fuseConfiguration;
    private final UploadQueueService uploadQueueService;
    private final DownloadQueueService downloadQueueService;

    private CloudFS cloudFS;
    //TODO: handle multiple cloud storages
    private ArrayList<CloudStorageService> cloudStorageServices = new ArrayList<>();
    private boolean alreadyInitialized = false;

    @Autowired
    public CloudFileSystemService(
            FuseConfiguration fuseConfiguration,
            UploadQueueService uploadQueueService,
            DownloadQueueService downloadQueueService) {
        this.fuseConfiguration = fuseConfiguration;
        this.uploadQueueService = uploadQueueService;
        this.downloadQueueService = downloadQueueService;
    }

    public void init() throws MakeRootException, IllegalAccessException {
        if (alreadyInitialized)
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
    public void onDirectoryAdded(CloudDirectory parent, CloudDirectory directory) throws MakeDirectoryException {
        cloudStorageServices.forEach(css -> {
                    try {
                        css.makeDirectory(parent, directory);
                    } catch (MakeDirectoryException e) {
                        e.printStackTrace();
                    }
                }
        );
    }

    @Override
    public void onDirectoryRemoved(CloudDirectory directory) throws RemoveDirectoryException {
        cloudStorageServices.stream()
                .findAny()
                .orElseThrow(() -> new RemoveDirectoryException(new CloudStorageServiceNotFound()))
                .removeDirectory(directory);
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
        cloudStorageServices.stream()
                .findAny()
                .orElseThrow(() -> new CreateFileException(new CloudStorageServiceNotFound()))
                .createFile(parent, file);
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
    public void onFileChanged(CloudFile file, long writeOffset, byte[] bytesToWrite) throws UploadFileException {
        uploadQueueService.queueFile(
                new UploadQueueItem(
                        cloudStorageServices.stream()
                                .findAny()
                                .orElseThrow(() -> new UploadFileException(new CloudStorageServiceNotFound())),
                        file,
                        writeOffset,
                        bytesToWrite
                )
        );
        logger.info("File modified");
    }

    @Override
    public void onFileRemoved(CloudFile file) throws RemoveFileException {
        cloudStorageServices.stream()
                .findAny()
                .orElseThrow(() -> new RemoveFileException(new CloudStorageServiceNotFound()))
                .removeFile(file);
        logger.info("File removed");
    }

    public void onRootMounted(CloudDirectory root) throws MakeRootException {
        cloudStorageServices.stream()
                .findAny()
                .orElseThrow(() -> new MakeRootException(new CloudStorageServiceNotFound()))
                .makeRoot(root, fuseConfiguration);
    }

    @Override
    public void synchronizeChildrenPaths(CloudDirectory directory) throws SynchronizeChildrenException {
        cloudStorageServices.stream()
                .findAny()
                .orElseThrow(() -> new SynchronizeChildrenException(new CloudStorageServiceNotFound()))
                .synchronizeChildrenPaths(directory);
    }

    @Override
    public long fileSize(CloudFile file) throws FileSizeRequestException {
        return file.getCloudPathInfo().getFileSize();
    }

    @Override
    public void onFileTruncated(CloudFile file, long size) throws TruncateFileException {
        cloudStorageServices.stream()
                .findAny()
                .orElseThrow(() -> new TruncateFileException(new CloudStorageServiceNotFound()))
                .truncateFile(file, size);
        logger.info("File truncated");
    }

    @Override
    public int onFileRead(CloudFile file, byte[] bytesRead, long offset, int bytesToRead) throws DownloadFileException {
        CompletableFuture<Integer> futureTask = downloadQueueService.queueFile(
                new DownloadQueueItem(
                        cloudStorageServices.stream()
                                .findAny()
                                .orElseThrow(() -> new DownloadFileException(new CloudStorageServiceNotFound())),
                        file, bytesRead, offset, bytesToRead)
        );
        try {
            int resultBytes = futureTask.get();
            logger.info("File downloaded");
            return resultBytes;
        } catch (InterruptedException | ExecutionException e) {
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
