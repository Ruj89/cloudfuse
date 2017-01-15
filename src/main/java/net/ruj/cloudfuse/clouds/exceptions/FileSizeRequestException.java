package net.ruj.cloudfuse.clouds.exceptions;

public class FileSizeRequestException extends Exception {
    public FileSizeRequestException(Exception e) {
        initCause(e);
    }
}
