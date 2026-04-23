package org.bazar.authorization.sdk;

/**
 * Exception thrown when authorization check fails or an error occurs during authorization
 */
public class AuthorizationException extends RuntimeException {
    public AuthorizationException(String message) {
        super(message);
    }

    public AuthorizationException(String message, Throwable cause) {
        super(message, cause);
    }

    public AuthorizationException(Throwable cause) {
        super(cause);
    }
}

