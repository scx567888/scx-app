package dev.scx.app.x.crud.serialization;

public class DeserializationException extends Exception {

    public DeserializationException(Throwable cause) {
        super(cause);
    }

    public DeserializationException(String message) {
        super(message);
    }

}
