package com.github.nagyesta.lowkeyvault.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static com.github.nagyesta.lowkeyvault.filter.PortSeparationFilter.PRECEDENCE;

@Component
@Slf4j
@Order(PRECEDENCE)
public class PortSeparationFilter
        extends OncePerRequestFilter {

    static final int PRECEDENCE = 50;

    @Override
    protected void doFilterInternal(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final FilterChain filterChain)
            throws ServletException, IOException {
        final var secure = request.isSecure();
        final var isPingRequest = request.getRequestURI().equals("/ping");
        final var isTokenRequest = request.getRequestURI().startsWith("/metadata/");
        final var unsecureTokenRequest = isTokenRequest && !secure;
        final var secureVaultRequest = !isTokenRequest && secure;
        if (isPingRequest || unsecureTokenRequest || secureVaultRequest) {
            filterChain.doFilter(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

}
