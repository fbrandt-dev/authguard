package io.cscenter.authguard.services;

import org.springframework.stereotype.Service;

import io.cscenter.authguard.error.exceptions.TokenAuthenticityException;
import io.cscenter.authguard.error.exceptions.TokenExpiredException;
import io.cscenter.shared.dto.enums.TokenValidityStatus;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class IRRService {

    private final OAuthTokenService tokenService;

    public IRRService(OAuthTokenService tokenService) {
        this.tokenService = tokenService;
    }

    public TokenValidityStatus isTokenValid(String jws) {

        try {
            this.tokenService.parseAccessToken(jws);
            return TokenValidityStatus.VALID;
        } catch (TokenAuthenticityException e) {
            return TokenValidityStatus.INVALID;

        } catch (TokenExpiredException e) {
            return TokenValidityStatus.EXPIRED;

        }
    }

}
