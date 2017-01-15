package net.ruj.cloudfuse.clouds.exceptions;

public class SynchronizeChildremException extends Exception {
    public SynchronizeChildremException(Exception e) {
        this.initCause(e);
    }
}
