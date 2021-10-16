package com.github.nagyesta.lowkeyvault.http;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;

import java.net.URISyntaxException;
import java.net.URL;
import java.util.Objects;

final class ApacheHttpRequest extends HttpEntityEnclosingRequestBase {
    private final String method;

    ApacheHttpRequest(final HttpMethod method, final URL url, final HttpHeaders headers) throws URISyntaxException {
        this.method = method.name();
        setURI(Objects.requireNonNull(url).toURI());
        headers.stream().forEach(header -> addHeader(header.getName(), header.getValue()));
    }

    @Override
    public String getMethod() {
        return method;
    }
}
