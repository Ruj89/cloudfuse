package net.ruj.cloudfuse.clouds.exceptions;

public class SynchronizeChildrenException extends Exception {
    public SynchronizeChildrenException(Exception e) {
        this.initCause(e);
    }
}
