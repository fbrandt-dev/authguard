package io.cscenter.authguard.error.exceptions;

import io.cscenter.shared.dto.enums.Domain;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@Builder
public class UsernameAlreadyTakenInDomainException extends RuntimeException {

    private final Domain domain;
    private final String username;

}
