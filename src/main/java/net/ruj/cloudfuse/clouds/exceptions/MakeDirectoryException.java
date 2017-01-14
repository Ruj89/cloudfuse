package net.ruj.cloudfuse.clouds.exceptions;

public class MakeDirectoryException extends Exception {
    public MakeDirectoryException(Exception e) {
        this.initCause(e);
    }
}
