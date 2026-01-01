package com.github.nagyesta.lowkeyvault.filter;

import com.github.nagyesta.lowkeyvault.model.common.ApiConstants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.URI;
import java.util.Optional;

@Component
@ConditionalOnExpression("${LOWKEY_ENABLE_AUTH}")
@Order(CommonAuthHeaderFilter.PRECEDENCE)
@Slf4j
public class CommonAuthHeaderFilter
        extends OncePerRequestFilter {

    static final int PRECEDENCE = 125;

    static final String OMIT_DEFAULT = "";
    private static final String HTTPS = "https://";
    private static final String BEARER_FAKE_TOKEN = "Bearer resource=\"%s\", authorization_uri=\"%s\"";
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();
    private final String authResource;

    public CommonAuthHeaderFilter(
            @Value("${LOWKEY_AUTH_RESOURCE:}") final String authResource) {
        log.info("Authentication is enforced.");
        this.authResource = authResource;
    }

    @Override
    protected void doFilterInternal(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final FilterChain filterChain) throws ServletException, IOException {
        final var baseUri = (URI) request.getAttribute(ApiConstants.REQUEST_BASE_URI);
        log.debug("Adding fake authenticate header to response for request: {}", request.getRequestURI());
        final var authResourceUri = Optional.of(authResource)
                .filter(anObject -> !OMIT_DEFAULT.equals(anObject))
                .map(res -> URI.create(HTTPS + res))
                .orElse(baseUri);
        response.setHeader(HttpHeaders.WWW_AUTHENTICATE,
                String.format(BEARER_FAKE_TOKEN, authResourceUri, baseUri + request.getRequestURI()));
        if (!StringUtils.hasText(request.getHeader(HttpHeaders.AUTHORIZATION))) {
            log.info("Sending token to client without processing payload: {}", request.getRequestURI());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(final HttpServletRequest request) {
        return ApiConstants.NON_VAULT_URIS.stream()
                .anyMatch(pattern -> antPathMatcher.matchStart(pattern, request.getRequestURI()));
    }

}
