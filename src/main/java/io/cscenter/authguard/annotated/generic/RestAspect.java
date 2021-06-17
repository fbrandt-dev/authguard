package io.cscenter.authguard.annotated.generic;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.annotation.Configuration;

public abstract class RestAspect {

    public final HttpServletRequest request;
    public final HttpServletResponse response;

    public RestAspect(final HttpServletRequest request, final HttpServletResponse response) {
        this.request = request;
        this.response = response;
    }

}
