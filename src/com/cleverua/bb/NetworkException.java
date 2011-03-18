package com.cleverua.bb;


public class NetworkException extends Exception {
    private Throwable cause;

    public NetworkException(Throwable cause) {
        super();
        this.cause = cause;
    }

    public NetworkException(String message) {
        super();
        this.cause = new Throwable(message);
    }

    public Throwable getCause() {
        return cause;
    }

    public String getMessage() {
        String message = cause.getMessage();
        if (StringUtils.isBlank(message)) {
            message = cause.toString();
        }
        return message;
    }

    public String toString() {
        return getMessage();
    }
}
