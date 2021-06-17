package io.cscenter.authguard.config;

import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Component;

import io.cscenter.shared.dto.CustomerIdentifierDTO;
import io.cscenter.shared.dto.OAuthTokenDataDTO;
import io.cscenter.shared.dto.enums.Domain;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.JwtHandler;

@Component
public class JwtOauthTokenDataHandler implements JwtHandler<OAuthTokenDataDTO> {

    @Override
    public OAuthTokenDataDTO onPlaintextJwt(Jwt<Header, String> jwt) {

        return null;
    }

    @Override
    public OAuthTokenDataDTO onClaimsJwt(Jwt<Header, Claims> jwt) {
        return null;
    }

    @Override
    public OAuthTokenDataDTO onPlaintextJws(Jws<String> jws) {
        return null;
    }

    @Override
    public OAuthTokenDataDTO onClaimsJws(Jws<Claims> jws) {

        Claims claims = jws.getBody();

        @SuppressWarnings("unchecked")
        Set<String> scopes = Set.copyOf(claims.get("scopes", ArrayList.class));

        return OAuthTokenDataDTO.builder().subject(claims.getSubject())
                .customerIdentifier(CustomerIdentifierDTO.builder()
                        .identifier(UUID.fromString(claims.get("customer_identifier", String.class)))
                        .domain(Domain.convert(claims.get("customer_domain", String.class))).build())
                .scopes(scopes).build();
    }

}