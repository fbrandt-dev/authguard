package io.cscenter.authguard.services;

import java.security.KeyPair;
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
    private static final int TOKEN_LIFETIME = 1800; // 30 minutes

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

        final UUID customerIdentifier = customer.getIdentifier();
        final Domain customerDomain = customer.getDomain();

        final Optional<CustomerEntity> optionalCustomerEntity = this.customerRepository
                .findById(CustomerIdentifier.builder().domain(customerDomain).identifier(customerIdentifier).build());

        if (optionalCustomerEntity.isPresent()) {

            final OAuthTokenEntity tokenEntity = this.generateTokens(optionalCustomerEntity.get(), scopes);

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

    public OAuthTokenDTO prolongAccessToken(final String jws) {

        final OAuthTokenDataDTO data = this.parseAccessToken(jws);
        final Domain customerDomain = data.getCustomerIdentifier().getDomain();
        final String customerUsername = data.getSubject();
        final Set<String> scopes = data.getScopes();

        final Optional<CustomerEntity> customer = this.customerRepository
                .findByCustomerIdentifier_DomainAndUsername(customerDomain, customerUsername);

        if (customer.isEmpty()) {
            return null;
        }

        OAuthTokenEntity createdToken = this.generateTokens(customer.get(), scopes);
        return this.tokenRepository.save(createdToken).transform();
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

    private OAuthTokenEntity generateTokens(final CustomerEntity customer, final Set<String> scopes) {
        final KeyPair key = Keys.keyPairFor(SignatureAlgorithm.PS512);

        CustomerIdentifier customerIdentification = customer.getCustomerIdentifier();

        final UUID customerIdentifier = customerIdentification.getIdentifier();
        final Domain customerDomain = customerIdentification.getDomain();

        final Instant instant = Instant.now();

        final Date refresh_token_issued_at = Date.from(instant);
        final Date refresh_token_expiration_date = Date.from(instant.plusSeconds(TOKEN_LIFETIME * 4));

        final Date access_token_issued_at = Date.from(instant);
        final Date access_token_expiration_date = Date.from(instant.plusSeconds(TOKEN_LIFETIME));

        final String access_token = Jwts.builder().setSubject(customer.getUsername())
                .setIssuedAt(access_token_issued_at).setAudience(AUTHGUARD_SECURITY_GATEWAY)
                .setExpiration(access_token_expiration_date).setIssuer(AUTHGUARD_SECURITY_GATEWAY)
                .signWith(key.getPrivate()).claim("customer_identifier", customerIdentifier).claim("scopes", scopes)
                .claim("customer_domain", customerDomain.toString()).compact();

        final String refresh_token = Jwts.builder().setSubject(customer.getUsername())
                .setIssuedAt(refresh_token_issued_at).setAudience(AUTHGUARD_SECURITY_GATEWAY)
                .setExpiration(refresh_token_expiration_date).setIssuer(AUTHGUARD_SECURITY_GATEWAY)
                .signWith(key.getPrivate()).claim("customer_identifier", customerIdentifier)
                .claim("scopes", Set.of("REFRESH_TOKEN")).claim("customer_domain", customerDomain.toString()).compact();

        return OAuthTokenEntity.builder().customer(customer).key(key.getPublic()).refreshToken(refresh_token)
                .token(access_token).refreshTokenIssuedAt(refresh_token_issued_at)
                .refreshTokenValidUntil(refresh_token_expiration_date).tokenIssuedAt(access_token_issued_at)
                .tokenValidUntil(access_token_expiration_date).build();
    }

    public void delete(OAuthTokenEntity correspondingOAuthTokenEntity) {

        this.tokenRepository.delete(correspondingOAuthTokenEntity);

    }

}
