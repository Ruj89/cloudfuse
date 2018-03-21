package net.ruj.cloudfuse.cache.services;

import net.ruj.cloudfuse.cache.exceptions.BiasedStartingOffsetItemException;
import net.ruj.cloudfuse.cache.exceptions.FileNotCachedException;
import net.ruj.cloudfuse.cache.items.CacheItem;
import net.ruj.cloudfuse.clouds.exceptions.DownloadFileException;
import net.ruj.cloudfuse.fuse.filesystem.VirtualFile;
import net.ruj.cloudfuse.utils.PatchedInputStream;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;

@Service
//TODO: Limit cache size
public class CacheService {
    private ArrayList<CacheItem> items = new ArrayList<>();

    public void storeItemChanges(VirtualFile file, byte[] bytes, long offset)
            throws IOException, BiasedStartingOffsetItemException {
        CacheItem item = new CacheItem(file, bytes, offset);
        Optional<CacheItem> replacingItemO = itemThatShouldBeReplaced(item);
        CacheItem finalItem;
        if (replacingItemO.isPresent()) {
            CacheItem replacingItem = replacingItemO.get();
            items.remove(replacingItem);
            finalItem = joinItems(replacingItem, item);
        } else {
            if (item.getOffset() != 0)
                throw new BiasedStartingOffsetItemException();
            finalItem = item;
        }
        items.add(finalItem);
    }

    private CacheItem joinItems(CacheItem baseItem, CacheItem patchItem) throws IOException {
        try (
                ByteArrayInputStream bais = new ByteArrayInputStream(baseItem.getBytes());
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                PatchedInputStream pis = new PatchedInputStream(
                        bais,
                        patchItem.getBytes(),
                        Math.toIntExact(patchItem.getOffset())
                )
        ) {
            IOUtils.copy(pis, baos);
            return new CacheItem(baseItem.getFile(), baos.toByteArray(), 0);
        }
    }

    private Optional<CacheItem> itemThatShouldBeReplaced(CacheItem item) {
        return getItemByFile(item.getFile());
    }

    private Optional<CacheItem> getItemByFile(VirtualFile file) {
        return items.stream()
                .filter(i -> i.getFile().equals(file))
                .findAny();
    }

    private Optional<CacheItem> contains(VirtualFile file, long offset, int bytesToRead) {
        return getItemByFile(file)
                .filter(i -> i.containsRange(offset, bytesToRead));
    }

    public int downloadCachedItem(VirtualFile file, byte[] bytesRead, long offset, int bytesToRead)
            throws DownloadFileException, FileNotCachedException {
        CacheItem cacheItem = contains(file, offset, bytesToRead)
                .orElseThrow(FileNotCachedException::new);
        try (ByteArrayInputStream bais = new ByteArrayInputStream(cacheItem.getBytes())) {
            return bais.read(
                    bytesRead,
                    Math.toIntExact(offset - cacheItem.getOffset()),
                    bytesToRead
            );
        } catch (IOException e) {
            throw new DownloadFileException(e);
        }
    }
}
