package net.ruj.cloudfuse.clouds.exceptions;

public class TruncateFileException extends Exception {
    public TruncateFileException(Exception e) {
        initCause(e);
    }
}
