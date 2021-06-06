package io.cscenter.authguard.http.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AccessTokenProlongiationResponse {

    private String token;

}
