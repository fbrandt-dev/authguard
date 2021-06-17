package io.cscenter.authguard.http.response;

import java.util.Date;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.cscenter.shared.dto.CustomerIdentifierDTO;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@Builder
@ToString
public class AuthenticationResponse {

    private UUID identifier;
    private CustomerIdentifierDTO customer;
    private String access_token;
    private String refresh_token;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm:ss", timezone = "Europe/Berlin")
    private Date access_token_expires_at;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm:ss", timezone = "Europe/Berlin")
    private Date refresh_token_expires_at;

}
