package io.cscenter.authguard.controllers;

import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.cscenter.authguard.annotated.Security;
import io.cscenter.authguard.data.OAuthTokenEntity;
import io.cscenter.authguard.data.SecurityContext;
import io.cscenter.authguard.http.request.AuthenticationRequest;
import io.cscenter.authguard.http.request.RefreshRequest;
import io.cscenter.authguard.http.request.RegistrationRequest;
import io.cscenter.authguard.http.response.AccessTokenProlongiationResponse;
import io.cscenter.authguard.http.response.AuthenticationResponse;
import io.cscenter.authguard.services.OAuthService;
import io.cscenter.shared.dto.CustomerIdentifierDTO;
import io.cscenter.shared.dto.OAuthTokenDTO;
import io.cscenter.shared.dto.enums.SecurityStatus;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/public/oauth")
@Slf4j
public class OAuthController {

    private final OAuthService customerService;

    public OAuthController(OAuthService customerService) {
        this.customerService = customerService;
    }

    @PostMapping("/register")
    @Security(securityStatus = SecurityStatus.UNAUTHENTICATED)
    public ResponseEntity<CustomerIdentifierDTO> createAccount(@RequestBody final RegistrationRequest request,
            SecurityContext context) {
        final String username = request.getUsername();
        final String password = request.getPassword();
        final String domain = request.getDomain();

        log.info("Received Registration Request for {} in Domain {}", username, domain);

        CustomerIdentifierDTO createdAccount = customerService.createAccount(username, password, domain);

        log.info("Completed Registration Request");

        return ResponseEntity.ok(createdAccount);
    }

    @PostMapping("/authenticate")
    @Security(securityStatus = SecurityStatus.UNAUTHENTICATED)
    public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody final AuthenticationRequest request) {

        final String username = request.getUsername();
        final String password = request.getPassword();
        final String domain = request.getDomain();

        log.info("Received Authentication Request for {} in Domain {}", username, domain);

        Optional<OAuthTokenDTO> possibleToken = this.customerService.authenticate(username, password, domain);

        if (possibleToken.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        OAuthTokenDTO oAuthTokenDTO = possibleToken.get();

        return ResponseEntity.ok(AuthenticationResponse.builder().access_token(oAuthTokenDTO.getAccess_token())
                .refresh_token(oAuthTokenDTO.getRefresh_token()).customer(oAuthTokenDTO.getCustomer())
                .identifier(oAuthTokenDTO.getIdentifier())
                .refresh_token_expires_at(oAuthTokenDTO.getRefresh_token_expires_at())
                .access_token_expires_at(oAuthTokenDTO.getAccess_token_expires_at()).build());
    }

    @PostMapping("/refresh")
    @Security(securityStatus = SecurityStatus.UNAUTHENTICATED)
    public ResponseEntity<AccessTokenProlongiationResponse> refresh(@RequestBody final RefreshRequest request) {

        OAuthTokenDTO prolongedAccessToken = this.customerService.refresh(request.getRefreshToken());

        return ResponseEntity
                .ok(AccessTokenProlongiationResponse.builder().access_token(prolongedAccessToken.getAccess_token())
                        .refresh_token(prolongedAccessToken.getRefresh_token())
                        .access_token_expires_at(prolongedAccessToken.getAccess_token_expires_at())
                        .refresh_token_expires_at(prolongedAccessToken.getRefresh_token_expires_at()).build());
    }

    @GetMapping("/status")
    @Security(securityStatus = SecurityStatus.IGNORED)
    public ResponseEntity<SecurityContext> getSecurityStatus(SecurityContext context) {
        return ResponseEntity.ok(context);
    }

    @PostMapping("/authorize")
    @Security(securityStatus = SecurityStatus.AUTHENTICATED)
    public ResponseEntity<?> authorize(SecurityContext context) {
        return ResponseEntity.badRequest().build();
    }

    @PostMapping("/logout")
    @Security(securityStatus = SecurityStatus.AUTHENTICATED)
    public ResponseEntity<?> logout(SecurityContext context) {
        return ResponseEntity.ok().build();
    }

}
