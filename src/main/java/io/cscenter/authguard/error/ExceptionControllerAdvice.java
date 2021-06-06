package io.cscenter.authguard.error;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import io.cscenter.authguard.error.exceptions.MalformedRegistrationRequestException;
import io.cscenter.authguard.error.exceptions.TokenAuthenticityException;
import io.cscenter.authguard.error.exceptions.TokenExpiredException;
import io.cscenter.authguard.error.exceptions.UsernameAlreadyTakenInDomainException;
import io.cscenter.authguard.http.response.ServiceFailure;
import io.cscenter.shared.dto.enums.HttpStatusType;

@ControllerAdvice
public class ExceptionControllerAdvice extends ResponseEntityExceptionHandler {

    @ExceptionHandler({ UsernameAlreadyTakenInDomainException.class })
    public ResponseEntity<ServiceFailure> handleUsernameAlreadyTakenInDomainException(
            UsernameAlreadyTakenInDomainException ex, WebRequest request) {

        HttpStatus status = HttpStatus.CONFLICT;

        Map<String, Object> data = Map.of("domain", ex.getDomain().toString(), "username", ex.getUsername());

        ServiceFailure response = ServiceFailure.builder()
                .languageKey(ErrorMessages.USERNAME_UNAVAILABLE_FOR_CURRENT_DOMAIN.toString()).data(data)
                .statusType(HttpStatusType.CLIENT).status(status).traceIdentifier(MDC.get("trace-identifier")).build();

        return ResponseEntity.status(status).body(response);

    }

    @ExceptionHandler({ MalformedRegistrationRequestException.class })
    public ResponseEntity<ServiceFailure> handleMalformedRegistrationRequestException(
            MalformedRegistrationRequestException ex, WebRequest request) {

        HttpStatus status = HttpStatus.BAD_REQUEST;

        Map<String, Object> data = new HashMap<>();

        if (ex.getDomain() == null) {
            data.put("incorrect", "domain");
        } else if (ex.getUsername().isBlank() == Boolean.TRUE) {
            data.put("incorrect", "username");
        } else {
            data.put("incorrect", "password");
        }

        ServiceFailure response = ServiceFailure.builder()
                .languageKey(ErrorMessages.MALFORMED_REGISTRATION_REQUEST.toString()).data(data)
                .statusType(HttpStatusType.CLIENT).status(status).traceIdentifier(MDC.get("trace-identifier")).build();

        return ResponseEntity.status(status).body(response);

    }

    @ExceptionHandler({ TokenAuthenticityException.class })
    public ResponseEntity<ServiceFailure> handleTokenAuthenticityFailure(TokenAuthenticityException ex,
            WebRequest request) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;

        Map<String, Object> data = new HashMap<>();

        ServiceFailure response = ServiceFailure.builder()
                .languageKey(ErrorMessages.TOKEN_AUTHENTICITY_CHECK_COULD_NOT_BE_COMPLETED.toString()).data(data)
                .statusType(HttpStatusType.CLIENT).status(status).traceIdentifier(MDC.get("trace-identifier")).build();

        return ResponseEntity.status(status).body(response);
    }

    @ExceptionHandler({ TokenExpiredException.class })
    public ResponseEntity<ServiceFailure> handleTokenExpiredFailure(TokenExpiredException ex, WebRequest request) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;

        Map<String, Object> data = new HashMap<>();

        ServiceFailure response = ServiceFailure.builder().languageKey(ErrorMessages.TOKEN_EXPIRED.toString())
                .data(data).statusType(HttpStatusType.CLIENT).status(status)
                .traceIdentifier(MDC.get("trace-identifier")).build();

        return ResponseEntity.status(status).body(response);
    }

}