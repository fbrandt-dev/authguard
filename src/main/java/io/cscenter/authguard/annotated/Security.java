package io.cscenter.authguard.annotated;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import io.cscenter.shared.dto.enums.SecurityStatus;

import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Security {

    String[] authorities() default {};

    SecurityStatus securityStatus() default SecurityStatus.IGNORED;

}
