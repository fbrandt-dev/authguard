package io.cscenter.authguard.http.response;

import io.cscenter.shared.dto.OAuthTokenDTO;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@Builder
@ToString
public class AuthenticationResponse {

    private OAuthTokenDTO tokens;

}
