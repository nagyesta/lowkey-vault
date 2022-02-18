package com.github.nagyesta.lowkeyvault.filter;

import com.github.nagyesta.lowkeyvault.model.common.ApiConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;

@Component
@Slf4j
public class CommonAuthHeaderFilter extends OncePerRequestFilter {

    private static final int DEFAULT_HTTPS_PORT = 443;
    private static final String OMIT_DEFAULT = "";
    private static final String PORT_SEPARATOR = ":";
    private static final String HTTPS = "https://";
    private static final String BEARER_FAKE_TOKEN = "Bearer resource=\"%s\", authorization_uri=\"%s\"";

    @Override
    protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response,
                                    @NonNull final FilterChain filterChain) throws ServletException, IOException {

        log.debug("Adding fake authenticate header to response for request: {}", request.getRequestURI());
        final String port = resolvePort(request.getServerPort());
        final URI baseUri = URI.create(HTTPS + request.getServerName() + port);
        request.setAttribute(ApiConstants.REQUEST_BASE_URI, baseUri);
        response.setHeader(HttpHeaders.WWW_AUTHENTICATE,
                String.format(BEARER_FAKE_TOKEN, request.getRequestURI(), request.getRequestURI()));
        if (!StringUtils.hasText(request.getHeader(HttpHeaders.AUTHORIZATION))) {
            log.info("Sending token to client without processing payload: {}", request.getRequestURI());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(final HttpServletRequest request) {
        return "/ping".equals(request.getRequestURI());
    }

    private String resolvePort(final int port) {
        if (port == DEFAULT_HTTPS_PORT) {
            return OMIT_DEFAULT;
        }
        return PORT_SEPARATOR + port;
    }
}
