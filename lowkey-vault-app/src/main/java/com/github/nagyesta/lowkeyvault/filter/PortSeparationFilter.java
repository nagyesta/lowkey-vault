package com.github.nagyesta.lowkeyvault.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Slf4j
public class PortSeparationFilter extends OncePerRequestFilter {

    @SuppressWarnings("NullableProblems")
    @Override
    protected void doFilterInternal(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final FilterChain filterChain)
            throws ServletException, IOException {
        final var secure = request.isSecure();
        final var isTokenRequest = request.getRequestURI().startsWith("/metadata/");
        final var unsecureTokenRequest = isTokenRequest && !secure;
        final var secureVaultRequest = !isTokenRequest && secure;
        if (unsecureTokenRequest || secureVaultRequest) {
            filterChain.doFilter(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

}
