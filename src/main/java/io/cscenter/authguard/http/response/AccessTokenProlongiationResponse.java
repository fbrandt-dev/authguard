package io.cscenter.authguard.http.response;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AccessTokenProlongiationResponse {

    private String access_token;
    private String refresh_token;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm:ss", timezone = "Europe/Berlin")
    private Date access_token_expires_at;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm:ss", timezone = "Europe/Berlin")
    private Date refresh_token_expires_at;

}
