package net.ruj.cloudfuse.cache;

import net.ruj.cloudfuse.cache.exceptions.BiasedStartingOffsetItemException;
import net.ruj.cloudfuse.cache.exceptions.FileNotCachedException;
import net.ruj.cloudfuse.cache.services.CacheService;
import net.ruj.cloudfuse.clouds.exceptions.DownloadFileException;
import net.ruj.cloudfuse.fuse.filesystem.CloudFile;
import org.apache.commons.lang3.ArrayUtils;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class CacheServiceTestUnit {
    private static final String HELLO_WORLD = "Hello world!";
    private static final String FOO_BAR = "Foo bar";

    @Test
    public void storeSingleItem()
            throws IOException, BiasedStartingOffsetItemException, FileNotCachedException, DownloadFileException {
        CacheService cacheService = new CacheService();
        CloudFile file = generateFakeFile("example");
        cacheService.storeItemChanges(file, HELLO_WORLD.getBytes(), 0);
        byte[] bytesRead = new byte[HELLO_WORLD.length()];
        cacheService.downloadCachedItem(file, bytesRead, 0, HELLO_WORLD.length());
        Assertions.assertThat(new String(bytesRead)).isEqualTo(HELLO_WORLD);
    }

    @Test
    public void storeSingleItemChunks()
            throws IOException, BiasedStartingOffsetItemException, FileNotCachedException, DownloadFileException {
        CacheService cacheService = new CacheService();
        CloudFile file = generateFakeFile("example");
        byte[] startingBytes = Arrays.copyOf(HELLO_WORLD.getBytes(), 5);
        byte[] endingBytes = Arrays.copyOfRange(HELLO_WORLD.getBytes(), 5, HELLO_WORLD.length());
        Assertions.assertThat(new String(ArrayUtils.addAll(startingBytes, endingBytes))).isEqualTo(HELLO_WORLD);
        cacheService.storeItemChanges(file, startingBytes, 0);
        cacheService.storeItemChanges(file, endingBytes, 5);
        byte[] bytesRead = new byte[HELLO_WORLD.length()];
        cacheService.downloadCachedItem(file, bytesRead, 0, HELLO_WORLD.length());
        Assertions.assertThat(new String(bytesRead)).isEqualTo(HELLO_WORLD);
    }

    @Test
    public void storeTwoDifferentItemsChunks()
            throws IOException, BiasedStartingOffsetItemException, FileNotCachedException, DownloadFileException {
        CacheService cacheService = new CacheService();
        CloudFile file1 = generateFakeFile("example1");
        CloudFile file2 = generateFakeFile("example2");
        byte[] startingBytes1 = Arrays.copyOf(HELLO_WORLD.getBytes(), 5);
        byte[] endingBytes1 = Arrays.copyOfRange(HELLO_WORLD.getBytes(), 5, HELLO_WORLD.length());
        Assertions.assertThat(new String(ArrayUtils.addAll(startingBytes1, endingBytes1))).isEqualTo(HELLO_WORLD);
        byte[] startingBytes2 = Arrays.copyOf(FOO_BAR.getBytes(), 5);
        byte[] endingBytes2 = Arrays.copyOfRange(FOO_BAR.getBytes(), 5, FOO_BAR.length());
        Assertions.assertThat(new String(ArrayUtils.addAll(startingBytes2, endingBytes2))).isEqualTo(FOO_BAR);
        cacheService.storeItemChanges(file1, startingBytes1, 0);
        cacheService.storeItemChanges(file2, startingBytes2, 0);
        cacheService.storeItemChanges(file1, endingBytes1, 5);
        cacheService.storeItemChanges(file2, endingBytes2, 5);
        byte[] bytesRead1 = new byte[HELLO_WORLD.length()];
        byte[] bytesRead2 = new byte[FOO_BAR.length()];
        cacheService.downloadCachedItem(file1, bytesRead1, 0, HELLO_WORLD.length());
        cacheService.downloadCachedItem(file2, bytesRead2, 0, FOO_BAR.length());
        Assertions.assertThat(new String(bytesRead1)).isEqualTo(HELLO_WORLD);
        Assertions.assertThat(new String(bytesRead2)).isEqualTo(FOO_BAR);
    }

    private CloudFile generateFakeFile(String name) {
        Path path = Paths.get(name);
        return new CloudFile(path, name, null);
    }
}
