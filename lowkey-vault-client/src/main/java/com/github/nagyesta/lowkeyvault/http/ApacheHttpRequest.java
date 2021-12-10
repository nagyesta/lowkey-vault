package com.github.nagyesta.lowkeyvault.http;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

final class ApacheHttpRequest extends HttpEntityEnclosingRequestBase {
    static final String LOCALHOST = "localhost";
    private final String method;

    ApacheHttpRequest(final HttpMethod method, final URL url,
                      final HttpHeaders headers, final Set<String> hostOverride) throws URISyntaxException {
        this.method = method.name();
        final URI uri = Objects.requireNonNull(url).toURI();
        if (Objects.requireNonNull(hostOverride).contains(uri.getHost())) {
            final String uriString = uri.toString().replaceFirst(Pattern.quote(uri.getHost()), LOCALHOST);
            setURI(URI.create(uriString));
        } else {
            setURI(uri);
        }
        headers.stream().forEach(header -> addHeader(header.getName(), header.getValue()));
        addHeader(org.apache.http.HttpHeaders.HOST, url.getAuthority());
    }

    @Override
    public String getMethod() {
        return method;
    }
}
