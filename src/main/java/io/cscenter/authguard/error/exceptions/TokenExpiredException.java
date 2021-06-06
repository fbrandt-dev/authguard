package io.cscenter.authguard.error.exceptions;

public class TokenExpiredException extends RuntimeException {

    public TokenExpiredException(final String message) {
        super(message);
    }

}
