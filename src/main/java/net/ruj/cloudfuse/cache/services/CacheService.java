package net.ruj.cloudfuse.cache.services;

import net.ruj.cloudfuse.cache.items.CacheItem;
import net.ruj.cloudfuse.clouds.exceptions.DownloadFileException;
import net.ruj.cloudfuse.fuse.filesystem.CloudFile;
import net.ruj.cloudfuse.queues.items.QueueItem;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;

@Service
public class CacheService {
    private ArrayList<CacheItem> items = new ArrayList<>();

    public void addQueueItem(QueueItem queueItem, byte[] bytes, long offset) {
        CacheItem item = new CacheItem(queueItem, bytes, offset);
        Optional<CacheItem> replacingItem = itemThatShouldBeReplaced(item);
        replacingItem.ifPresent(cacheItem -> items.remove(cacheItem));
        items.add(item);
    }

    private Optional<CacheItem> itemThatShouldBeReplaced(CacheItem item) {
        return getItemByFile(item.getFile());
    }

    private Optional<CacheItem> getItemByFile(CloudFile file) {
        return items.stream()
                .filter(i -> i.getFile().equals(file))
                .findAny();
    }

    public Optional<CacheItem> contains(CloudFile file, long offset, int bytesToRead) {
        return getItemByFile(file)
                .filter(i -> i.containsRange(offset, bytesToRead));
    }

    public int downloadCachedItem(CacheItem cachedItem, byte[] bytesRead, long offset, int bytesToRead)
            throws DownloadFileException {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(cachedItem.getBytes())) {
            return bais.read(
                    bytesRead,
                    Math.toIntExact(offset - cachedItem.getOffset()),
                    bytesToRead
            );
        } catch (IOException e) {
            throw new DownloadFileException(e);
        }
    }
}
