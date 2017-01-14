package net.ruj.cloudfuse.clouds.exceptions;

public class MakeRootException extends Exception {
    public MakeRootException(Exception e) {
        this.initCause(e);
    }
}
