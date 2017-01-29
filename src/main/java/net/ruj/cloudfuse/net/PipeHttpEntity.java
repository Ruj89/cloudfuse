package net.ruj.cloudfuse.net;

import org.apache.commons.io.IOUtils;
import org.apache.http.entity.AbstractHttpEntity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class PipeHttpEntity extends AbstractHttpEntity {
    private InputStream inputStream;
    private long contentLength = -1;

    public PipeHttpEntity(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public PipeHttpEntity(InputStream inputStream, long contentLength) {
        this.inputStream = inputStream;
        this.contentLength = contentLength;
    }

    @Override
    public boolean isRepeatable() {
        return false;
    }

    @Override
    public long getContentLength() {
        return contentLength;
    }

    @Override
    public InputStream getContent() throws IOException, UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeTo(OutputStream outstream) throws IOException {
        IOUtils.copyLarge(inputStream, outstream);
    }

    @Override
    public boolean isStreaming() {
        return false;
    }
}
