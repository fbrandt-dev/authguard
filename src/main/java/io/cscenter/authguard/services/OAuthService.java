package io.cscenter.authguard.services;

import java.security.PrivateKey;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;

import io.cscenter.authguard.data.CustomerEntity;
import io.cscenter.authguard.data.CustomerIdentifier;
import io.cscenter.authguard.data.OAuthTokenEntity;
import io.cscenter.authguard.data.repositories.CustomerEntityRepository;
import io.cscenter.authguard.data.repositories.OAuthTokenEntityRepository;
import io.cscenter.authguard.error.exceptions.UsernameAlreadyTakenInDomainException;
import io.cscenter.shared.dto.CustomerIdentifierDTO;
import io.cscenter.shared.dto.OAuthTokenDTO;
import io.cscenter.shared.dto.enums.Domain;
import io.cscenter.shared.helpers.Randomize;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class OAuthService {

    private final CustomerEntityRepository customerEntityRepository;
    private final OAuthTokenEntityRepository oAuthTokenEntityRepository;
    private final OAuthTokenService oAuthTokenService;

    public OAuthService(CustomerEntityRepository customerEntityRepository,
            OAuthTokenEntityRepository oAuthTokenEntityRepository, OAuthTokenService oAuthTokenService) {
        this.customerEntityRepository = customerEntityRepository;
        this.oAuthTokenEntityRepository = oAuthTokenEntityRepository;
        this.oAuthTokenService = oAuthTokenService;
    }

    public CustomerIdentifierDTO createAccount(String username, String password, String domainString)
            throws UsernameAlreadyTakenInDomainException {

        final Domain domain = Domain.convert(domainString);

        Optional<CustomerEntity> possibleAlreadyExistingAccount = this.customerEntityRepository
                .findByCustomerIdentifier_DomainAndUsername(domain, username);

        if (possibleAlreadyExistingAccount.isPresent()) {
            log.info("Won't create account due to already existing username {}", username);
            throw UsernameAlreadyTakenInDomainException.builder().domain(domain).username(username).build();
        }

        CustomerEntity customerData = CustomerEntity.builder()
                .customerIdentifier(CustomerIdentifier.builder().domain(domain)
                        .identifier(getFreeCustomerIdentifier(domain)).build())
                .username(username).password(password).build();

        log.info("Successfully created account {}", customerData.transform());

        return this.customerEntityRepository.save(customerData).transform();
    }

    public Optional<OAuthTokenDTO> authenticate(String username, String password, String domainString) {

        final Domain domain = Domain.convert(domainString);

        Optional<CustomerEntity> possibleCustomer = this.customerEntityRepository
                .findByCustomerIdentifier_DomainAndUsername(domain, username);
        if (possibleCustomer.isEmpty()) {
            return Optional.empty();
        }

        OAuthTokenDTO token = this.oAuthTokenService.createOAuthToken(possibleCustomer.get().transform(),
                Set.of("OAUTH_REFRESH", "OAUTH_INVALIDATE", "SCOPES_LIST"));

        return Optional.of(token);
    }

    private UUID getFreeCustomerIdentifier(final Domain domain) {
        UUID generatedIdentifier = UUID.randomUUID();
        Optional<CustomerEntity> findById = this.customerEntityRepository
                .findById(CustomerIdentifier.builder().domain(domain).identifier(generatedIdentifier).build());

        if (findById.isPresent()) {
            log.info("Found existing Identifier for Customer with Domain " + domain);
            return getFreeCustomerIdentifier(domain);
        }
        return generatedIdentifier;

    }

    public String refresh(String refreshToken) {

        OAuthTokenEntity correspondingOAuthTokenEntity = this.oAuthTokenService
                .getCorrespondingOAuthTokenEntityByRefreshToken(refreshToken);

        final String accessToken = correspondingOAuthTokenEntity.getToken();

        final PrivateKey key = correspondingOAuthTokenEntity.getPrivateKey();

        final String prolongedAccessToken = this.oAuthTokenService.prolongAccessToken(accessToken, key);

        correspondingOAuthTokenEntity.setToken(prolongedAccessToken);

        this.oAuthTokenEntityRepository.save(correspondingOAuthTokenEntity);

        return prolongedAccessToken;
    }

}
