package net.ruj.cloudfuse.clouds.exceptions;

public class CreateFileException extends Exception {
    public CreateFileException(Exception e) {
        this.initCause(e);
    }
}
