package io.cscenter.authguard.error.exceptions;

public class TokenAuthenticityException extends RuntimeException {

    public TokenAuthenticityException(final String message) {
        super(message);
    }

}
