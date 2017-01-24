package net.ruj.cloudfuse.net;

import org.apache.commons.io.IOUtils;
import org.apache.http.entity.AbstractHttpEntity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class PipeHttpEntity extends AbstractHttpEntity {
    private InputStream inputStream;

    public PipeHttpEntity(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    @Override
    public boolean isRepeatable() {
        return false;
    }

    @Override
    public long getContentLength() {
        return -1;
    }

    @Override
    public InputStream getContent() throws IOException, UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeTo(OutputStream outstream) throws IOException {
        IOUtils.copy(inputStream, outstream);
    }

    @Override
    public boolean isStreaming() {
        return false;
    }
}
