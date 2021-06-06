package io.cscenter.authguard.services;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;

import io.cscenter.authguard.config.JwtOauthTokenDataHandler;
import io.cscenter.authguard.data.CustomerEntity;
import io.cscenter.authguard.data.CustomerIdentifier;
import io.cscenter.authguard.data.OAuthTokenEntity;
import io.cscenter.authguard.data.repositories.CustomerEntityRepository;
import io.cscenter.authguard.data.repositories.OAuthTokenEntityRepository;
import io.cscenter.authguard.error.exceptions.TokenAuthenticityException;
import io.cscenter.authguard.error.exceptions.TokenExpiredException;
import io.cscenter.shared.dto.CustomerIdentifierDTO;
import io.cscenter.shared.dto.OAuthTokenDTO;
import io.cscenter.shared.dto.OAuthTokenDataDTO;
import io.cscenter.shared.dto.enums.Domain;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class OAuthTokenService {

    private static final String AUTHGUARD_SECURITY_GATEWAY = "authguard@security-gateway";

    private final OAuthTokenEntityRepository tokenRepository;
    private final CustomerEntityRepository customerRepository;

    private JwtOauthTokenDataHandler handler;

    public OAuthTokenService(final OAuthTokenEntityRepository tokenRepository,
            final CustomerEntityRepository customerRepository, JwtOauthTokenDataHandler handler) {
        this.tokenRepository = tokenRepository;
        this.customerRepository = customerRepository;
        this.handler = handler;
    }

    private String convertToPublicKey(KeyPair pair) {
        final String encodedPublicKey = Base64.getEncoder().encodeToString(pair.getPublic().getEncoded());
        final StringBuilder result = new StringBuilder();
        result.append("-----BEGIN PUBLIC KEY-----\n");
        result.append(encodedPublicKey);
        result.append("\n-----END PUBLIC KEY-----");
        return result.toString();
    }

    public OAuthTokenDTO createOAuthToken(final CustomerIdentifierDTO customer, Set<String> scopes) {
        final KeyPair key = Keys.keyPairFor(SignatureAlgorithm.PS512);

        final UUID customerIdentifier = customer.getIdentifier();
        final Domain customerDomain = customer.getDomain();
        final String subject = customerIdentifier.toString();

        final Optional<CustomerEntity> optionalCustomerEntity = this.customerRepository
                .findById(CustomerIdentifier.builder().domain(customerDomain).identifier(customerIdentifier).build());

        if (optionalCustomerEntity.isPresent()) {
            final String access_jws = Jwts.builder().setSubject(subject).setIssuedAt(Date.from(Instant.now()))
                    .setAudience(AUTHGUARD_SECURITY_GATEWAY).setExpiration(Date.from(Instant.now().plusSeconds(1800)))
                    .setIssuer(AUTHGUARD_SECURITY_GATEWAY).signWith(key.getPrivate())
                    .claim("customer_identifier", customerIdentifier).claim("scopes", scopes)
                    .claim("customer_domain", customerDomain.toString()).compact();

            final String refresh_jws = Jwts.builder().setSubject(subject).setIssuedAt(Date.from(Instant.now()))
                    .setAudience(AUTHGUARD_SECURITY_GATEWAY)
                    .setExpiration(Date.from(Instant.now().plusSeconds(1800 * 4))).setIssuer(AUTHGUARD_SECURITY_GATEWAY)
                    .signWith(key.getPrivate()).claim("customer_identifier", customerIdentifier)
                    .claim("customer_domain", customerDomain.toString()).claim("scopes", scopes).compact();

            final OAuthTokenEntity tokenEntity = OAuthTokenEntity.builder().customer(optionalCustomerEntity.get())
                    .key(key.getPublic()).privateKey(key.getPrivate()).token(access_jws).refreshToken(refresh_jws)
                    .build();

            this.tokenRepository.save(tokenEntity);

            log.info("Created new OAuthToken with Scopes: {} for Customer: {} in {}", scopes, customerIdentifier,
                    customerDomain);

            return tokenEntity.transform();
        }

        return null;
    }

    public OAuthTokenEntity getCorrespondingOAuthTokenEntityByRefreshToken(final String jws) {
        Optional<OAuthTokenEntity> possibleTokenEntity = this.tokenRepository.findByRefreshToken(jws);

        if (possibleTokenEntity.isEmpty()) {
            throw new TokenAuthenticityException("Could not verify Token!");
        }

        OAuthTokenEntity tokenEntity = possibleTokenEntity.get();

        try {

            Jwts.parserBuilder().setSigningKey(tokenEntity.getKey()).build().parse(jws, handler);
            return tokenEntity;
        } catch (ExpiredJwtException e) {
            throw new TokenExpiredException(e.getMessage());
        }
    }

    public String prolongAccessToken(final String jws, final PrivateKey key) {

        final OAuthTokenDataDTO data = this.parseAccessToken(jws);
        final UUID customerIdentifier = data.getCustomerIdentifier().getIdentifier();
        final Domain customerDomain = data.getCustomerIdentifier().getDomain();
        final Set<String> scopes = data.getScopes();

        final String access_jws = Jwts.builder().setSubject(customerIdentifier.toString())
                .setIssuedAt(Date.from(Instant.now())).setAudience(AUTHGUARD_SECURITY_GATEWAY)
                .setExpiration(Date.from(Instant.now().plusSeconds(1800))).setIssuer(AUTHGUARD_SECURITY_GATEWAY)
                .signWith(key).claim("customer_identifier", customerIdentifier).claim("scopes", scopes)
                .claim("customer_domain", customerDomain.toString()).compact();

        return access_jws;
    }

    public OAuthTokenDataDTO parseAccessToken(final String jws) {

        Optional<OAuthTokenEntity> possibleTokenEntity = this.tokenRepository.findByToken(jws);

        if (possibleTokenEntity.isEmpty()) {
            throw new TokenAuthenticityException("Could not verify Token!");
        }

        OAuthTokenEntity tokenEntity = possibleTokenEntity.get();

        try {

            return Jwts.parserBuilder().setSigningKey(tokenEntity.getKey()).build().parse(jws, handler);
        } catch (ExpiredJwtException e) {
            throw new TokenExpiredException(e.getMessage());
        }
    }

}
