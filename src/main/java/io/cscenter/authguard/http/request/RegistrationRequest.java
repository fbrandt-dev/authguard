package io.cscenter.authguard.http.request;

import lombok.Data;

@Data
public class RegistrationRequest {

    private String domain;
    private String username;
    private String password;

}
