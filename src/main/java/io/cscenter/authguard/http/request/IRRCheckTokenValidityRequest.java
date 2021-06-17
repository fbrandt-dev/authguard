package io.cscenter.authguard.http.request;

import lombok.Builder;
import lombok.Data;

@Data
public class IRRCheckTokenValidityRequest {

    private String token;

}
