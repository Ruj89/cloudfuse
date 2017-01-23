package net.ruj.cloudfuse.clouds.exceptions;

public class DownloadFileException extends Exception {
    public DownloadFileException(Exception e) {
        initCause(e);
    }
}
