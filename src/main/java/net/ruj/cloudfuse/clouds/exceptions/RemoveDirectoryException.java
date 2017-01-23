package net.ruj.cloudfuse.clouds.exceptions;

public class RemoveDirectoryException extends Exception {
    public RemoveDirectoryException(Exception e) {
        initCause(e);
    }
}
