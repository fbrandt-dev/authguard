package io.cscenter.authguard.http.request;

import lombok.Data;

@Data
public class AuthenticationRequest {

    private String domain;
    private String username;
    private String password;

}
