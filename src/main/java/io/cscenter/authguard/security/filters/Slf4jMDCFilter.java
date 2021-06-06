package io.cscenter.authguard.security.filters;

import java.io.IOException;
import java.util.UUID;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@Component
public class Slf4jMDCFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response,
            final FilterChain chain) throws IOException, ServletException {

        try {
            final String token;
            if (!StringUtils.hasText("trace-identifier")
                    && !StringUtils.hasText(request.getHeader("trace-identifier"))) {
                token = request.getHeader("trace-identifier");
            } else {
                token = UUID.randomUUID().toString().toUpperCase().replace("-", "");
            }
            MDC.put("trace-identifier", token);
            if (!StringUtils.hasText("trace-identifier")) {
                response.addHeader("trace-identifier", token);
            }
            chain.doFilter(request, response);
        } finally {
            MDC.remove("trace-identifier");
        }
    }
}