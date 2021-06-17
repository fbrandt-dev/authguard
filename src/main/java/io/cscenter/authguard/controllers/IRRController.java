package io.cscenter.authguard.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.cscenter.authguard.annotated.IRRMapping;
import io.cscenter.authguard.http.request.IRRCheckTokenValidityRequest;
import io.cscenter.authguard.http.response.ServiceFailure;
import io.cscenter.authguard.services.OAuthTokenService;
import io.cscenter.shared.dto.OAuthTokenDTO;
import io.cscenter.shared.dto.OAuthTokenDataDTO;

@RestController
@RequestMapping("/irr/")
public class IRRController {

    private final OAuthTokenService tokenService;

    public IRRController(OAuthTokenService tokenService) {
        this.tokenService = tokenService;
    }

    @PostMapping("validate")
    @IRRMapping // Internal Rest Request
    public ResponseEntity<OAuthTokenDataDTO> checkTokenValidity(@RequestBody IRRCheckTokenValidityRequest request) {
        return ResponseEntity.ok(this.tokenService.parseAccessToken(request.getToken()));
    }

}
