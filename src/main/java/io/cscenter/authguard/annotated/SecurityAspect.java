package io.cscenter.authguard.annotated;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.annotation.Configuration;

import io.cscenter.authguard.data.OAuthTokenEntity;
import io.cscenter.authguard.data.SecurityContext;
import io.cscenter.authguard.data.repositories.OAuthTokenEntityRepository;
import io.cscenter.authguard.services.OAuthTokenService;
import io.cscenter.shared.dto.OAuthTokenDataDTO;
import io.cscenter.shared.dto.enums.SecurityStatus;
import lombok.extern.slf4j.Slf4j;

@Aspect
@Configuration
@Slf4j
public class SecurityAspect {

    private HttpServletRequest request;
    private final OAuthTokenService tokenService;
    private final OAuthTokenEntityRepository repository;
    private HttpServletResponse response;

    public SecurityAspect(HttpServletRequest request, HttpServletResponse response, OAuthTokenService tokenService,
            OAuthTokenEntityRepository repository) {
        this.tokenService = tokenService;
        this.request = request;
        this.response = response;
        this.repository = repository;
    }

    @Around("@annotation(io.cscenter.authguard.annotated.Security)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        Method method = MethodSignature.class.cast(joinPoint.getSignature()).getMethod();

        Security annotation = method.getAnnotation(Security.class);

        Object[] args = joinPoint.getArgs();

        final String authorizationHeader = this.request.getHeader("Authorization");

        if (annotation.securityStatus() == SecurityStatus.AUTHENTICATED && authorizationHeader == null) {
            log.info("Ignored Request due to missing authorizationHeader");
            this.response.sendError(401);
            return null;
        }

        SecurityContext securityContext = SecurityContext.builder().scopes(Set.of()).build();

        Optional<OAuthTokenEntity> optionalToken = Optional.empty();

        if (authorizationHeader != null) {

            final String normalizedAuthorizationHeader = authorizationHeader.replace("Bearer ", "");
            OAuthTokenDataDTO parsed = this.tokenService.parseAccessToken(normalizedAuthorizationHeader);

            Optional<OAuthTokenEntity> maybeToken = this.repository.findByToken(normalizedAuthorizationHeader);

            if (maybeToken.isPresent()) {
                OAuthTokenEntity token = maybeToken.get();

                if (token.getCustomer().transform().getDomain().equals(parsed.getCustomerIdentifier().getDomain())
                        && token.getCustomer().transform().getIdentifier()
                                .equals(parsed.getCustomerIdentifier().getIdentifier())) {

                    securityContext.setScopes(parsed.getScopes());
                    securityContext.setAuthenticatedCustomer(token.getCustomer());

                }
            }

        }

        if (annotation.securityStatus() == SecurityStatus.AUTHENTICATED && optionalToken.isEmpty())

        {
            log.info("Ignoring Request due to invalid authorizationHeader {}", authorizationHeader);
            this.response.sendError(401);
            return null;
        }

        if (annotation.securityStatus() == SecurityStatus.UNAUTHENTICATED && optionalToken.isPresent()) {
            OAuthTokenEntity token = optionalToken.get();

            log.info(
                    "Ignoring Request because an Authenticated Customer ({},{}) tries to execute a method, which is only allowed to Unauthenticated Customers!",
                    token.getCustomer().getCustomerIdentifier().getIdentifier(),
                    token.getCustomer().getCustomerIdentifier().getDomain());
            this.response.sendError(403);
            return null;
        }

        if (optionalToken.isPresent()) {
            OAuthTokenEntity token = optionalToken.get();
            securityContext.setAuthenticatedCustomer(token.getCustomer());
        }

        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            if (arg.getClass().isAssignableFrom(SecurityContext.class)) {
                args[i] = securityContext;
            }
        }

        return joinPoint.proceed(args);
    }

}
