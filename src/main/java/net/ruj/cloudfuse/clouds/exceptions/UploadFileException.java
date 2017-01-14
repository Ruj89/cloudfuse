package net.ruj.cloudfuse.clouds.exceptions;

public class UploadFileException extends Exception {
    public UploadFileException(Exception e) {
        this.initCause(e);
    }
}
