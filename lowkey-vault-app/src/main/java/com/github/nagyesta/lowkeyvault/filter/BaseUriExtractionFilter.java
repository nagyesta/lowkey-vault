package com.github.nagyesta.lowkeyvault.filter;

import com.github.nagyesta.lowkeyvault.model.common.ApiConstants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.URI;

@Component
@Order(BaseUriExtractionFilter.PRECEDENCE)
@Slf4j
public class BaseUriExtractionFilter
        extends OncePerRequestFilter {

    static final int PRECEDENCE = 100;

    static final String OMIT_DEFAULT = "";
    private static final int DEFAULT_HTTPS_PORT = 443;
    private static final String PORT_SEPARATOR = ":";
    private static final String HTTPS = "https://";
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Override
    protected void doFilterInternal(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final FilterChain filterChain) throws ServletException, IOException {
        final var port = resolvePort(request.getServerPort());
        final var baseUri = URI.create(HTTPS + request.getServerName() + port);
        request.setAttribute(ApiConstants.REQUEST_BASE_URI, baseUri);
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(final HttpServletRequest request) {
        return ApiConstants.NON_VAULT_URIS.stream()
                .anyMatch(pattern -> antPathMatcher.matchStart(pattern, request.getRequestURI()));
    }

    private String resolvePort(final int port) {
        if (port == DEFAULT_HTTPS_PORT) {
            return OMIT_DEFAULT;
        }
        return PORT_SEPARATOR + port;
    }
}
