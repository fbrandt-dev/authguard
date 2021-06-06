package io.cscenter.authguard.http.request;

import lombok.Data;

@Data
public class RefreshRequest {

    private String refreshToken;

}
