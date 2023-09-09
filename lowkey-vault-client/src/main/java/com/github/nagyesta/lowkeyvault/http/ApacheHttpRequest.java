package com.github.nagyesta.lowkeyvault.http;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Objects;
import java.util.function.Function;


/**
 * Modified class based on <a href="https://github.com/Azure/azure-sdk-for-java/wiki/Custom-HTTP-Clients">Azure SDK wiki</a>.
 */
final class ApacheHttpRequest extends HttpEntityEnclosingRequestBase {
    private final String method;

    /**
     * Creates a new request used by the HTTP client. Sets all parameters, including the HOST
     * header to preserve the original authority passed in the logical URL.
     *
     * @param method                    The method of the HTTP request.
     * @param url                       The logical URL used by Lowkey-Vault to identify the resource.
     * @param headers                   The headers of the request.
     * @param authorityOverrideFunction The function which may override the authority part of the URL.
     *                                  e.g. If the original URL uses vault.localhost:8443 as authority,
     *                                  but this hostname is not known by the host machine (or the port is mapped
     *                                  to a non-default port), it can map it to localhost:30443 or 127.0.0.1:30443
     *                                  and send the request there just like a proxy would.
     * @throws URISyntaxException When the URI is malformed.
     */
    ApacheHttpRequest(final HttpMethod method, final URL url,
                      final HttpHeaders headers, final Function<URI, URI> authorityOverrideFunction) throws URISyntaxException {
        this.method = method.name();
        final URI uri = Objects.requireNonNull(url).toURI();
        final URI overrideUri = Objects.requireNonNull(authorityOverrideFunction).apply(uri);
        setURI(overrideUri);
        headers.stream().forEach(header -> addHeader(header.getName(), header.getValue()));
        addHeader(org.apache.http.HttpHeaders.HOST, url.getAuthority());
    }

    @Override
    public String getMethod() {
        return method;
    }
}
