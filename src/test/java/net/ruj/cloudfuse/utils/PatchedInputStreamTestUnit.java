package net.ruj.cloudfuse.utils;

import org.apache.commons.io.IOUtils;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;

import static java.nio.charset.StandardCharsets.UTF_8;

public class PatchedInputStreamTestUnit {
    @Test
    public void patchedInputStreamTestEnd() throws Exception {
        byte[] replacement = "alternative world!".getBytes();
        try (
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream("Hello world!".getBytes());
                PatchedInputStream patchedInputStream = new PatchedInputStream(byteArrayInputStream, replacement, 6)
        ) {
            StringWriter writer = new StringWriter();
            IOUtils.copy(patchedInputStream, writer, UTF_8);
            Assertions.assertThat(writer.toString()).isEqualTo("Hello alternative world!");
            Assertions.assertThat(patchedInputStream.calculateSize(12)).isEqualTo(24);
        }
    }

    @Test
    public void patchedInputStreamTestStart() throws Exception {
        byte[] replacement = "Fu** ".getBytes();
        try (
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream("Hello world!".getBytes());
                PatchedInputStream patchedInputStream = new PatchedInputStream(byteArrayInputStream, replacement, 0)
        ) {
            StringWriter writer = new StringWriter();
            IOUtils.copy(patchedInputStream, writer, UTF_8);
            Assertions.assertThat(writer.toString()).isEqualTo("Fu**  world!");
            Assertions.assertThat(patchedInputStream.calculateSize(12)).isEqualTo(12);
        }
    }

    @Test
    public void patchedInputStreamTestMiddle() throws Exception {
        byte[] replacement = "y :)".getBytes();
        try (
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream("Hello world!".getBytes());
                PatchedInputStream patchedInputStream = new PatchedInputStream(byteArrayInputStream, replacement, 2)
        ) {
            StringWriter writer = new StringWriter();
            IOUtils.copy(patchedInputStream, writer, UTF_8);
            Assertions.assertThat(writer.toString()).isEqualTo("Hey :)world!");
            Assertions.assertThat(patchedInputStream.calculateSize(12)).isEqualTo(12);
        }
    }

    @Test
    public void patchedInputStreamTestMiddleAndMore() throws Exception {
        byte[] replacement = "y :) fu**ing world!".getBytes();
        try (
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream("Hello world!".getBytes());
                PatchedInputStream patchedInputStream = new PatchedInputStream(byteArrayInputStream, replacement, 2)
        ) {
            StringWriter writer = new StringWriter();
            IOUtils.copy(patchedInputStream, writer, UTF_8);
            Assertions.assertThat(writer.toString()).isEqualTo("Hey :) fu**ing world!");
            Assertions.assertThat(patchedInputStream.calculateSize(12)).isEqualTo(21);
        }
    }
}
