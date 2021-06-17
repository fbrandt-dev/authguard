package io.cscenter.authguard.annotated;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import inet.ipaddr.AddressStringException;
import inet.ipaddr.IPAddress;
import inet.ipaddr.IPAddressString;
import inet.ipaddr.IncompatibleAddressException;
import io.cscenter.authguard.annotated.generic.RestAspect;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Aspect
@Configuration
public class IRRMappingAspect extends RestAspect {

    @Value("${irr.subnets.allowed}")
    private String[] allowedIRRSubnets = {};

    public IRRMappingAspect(HttpServletRequest request, HttpServletResponse response) {
        super(request, response);
    }

    @Around("@annotation(io.cscenter.authguard.annotated.IRRMapping)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {

        final String system = this.request.getHeader("X-cscenter-system");
        MDC.put("trace-identifier", system + " <-> " + MDC.get("trace-identifier"));

        if (system == null
                || this.isAddressInOneOfSubnet(this.request.getRemoteAddr(), this.allowedIRRSubnets) == false) {
            log.warn("[IRR] Incoming Request Routing denied from {}@{}:{} to route {} -> {} ({})",
                    request.getRemoteUser(), request.getRemoteHost(), request.getRemotePort(), request.getMethod(),
                    request.getRequestURI(), request.getProtocol());
            response.sendError(401);
            return null;
        }

        log.info("[IRR] Incoming Request Routing allowed from system {} to route {} -> {} ({})", system,
                request.getMethod(), request.getRequestURI(), request.getProtocol());

        return joinPoint.proceed();
    }

    private boolean isAddressInOneOfSubnet(String remoteAddr, String[] allowedSubnets)
            throws AddressStringException, IncompatibleAddressException {
        IPAddress ip = new IPAddressString(remoteAddr).toAddress();

        boolean partOf = false;

        for (String allowedSubnet : allowedSubnets) {
            IPAddress subnetAddress;
            try {
                subnetAddress = new IPAddressString(allowedSubnet).toAddress();
                if (partOf == false) {
                    partOf = subnetAddress.contains(ip);
                }
            } catch (AddressStringException | IncompatibleAddressException e) {
                log.warn(e.getMessage());
            }
        }

        log.info("The ip: {} is {} any of the subnets: {}", remoteAddr, partOf ? "part of" : "not included in",
                allowedSubnets);

        return partOf;
    }

}
