package com.github.nagyesta.lowkeyvault.http;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.FluxUtil;
import com.github.nagyesta.lowkeyvault.http.management.LowkeyVaultException;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import reactor.core.publisher.Mono;

import javax.net.ssl.HostnameVerifier;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;
import java.util.function.UnaryOperator;

/**
 * Modified class based on <a href="https://github.com/Azure/azure-sdk-for-java/wiki/Custom-HTTP-Clients">
 * https://github.com/Azure/azure-sdk-for-java/wiki/Custom-HTTP-Clients</a>.
 *
 * @param httpClient                the http client we will use.
 * @param authorityOverrideFunction The function mapping between the logical host name used by vault URLs
 *                                  and the host name used by the host machine for accessing Lowkey Vault.
 *                                  e.g., Maps from *.localhost:8443 to localhost:30443.
 */
public record ApacheHttpClient(
        org.apache.http.client.HttpClient httpClient,
        UnaryOperator<URI> authorityOverrideFunction) implements HttpClient {

    public ApacheHttpClient(
            final UnaryOperator<URI> authorityOverrideFunction,
            final TrustStrategy trustStrategy,
            final HostnameVerifier hostnameVerifier) {
        this(createHttpClient(trustStrategy, hostnameVerifier), Objects.requireNonNull(authorityOverrideFunction));
    }

    public ApacheHttpClient(
            final org.apache.http.client.HttpClient httpClient,
            final UnaryOperator<URI> authorityOverrideFunction) {
        this.httpClient = Objects.requireNonNull(httpClient);
        this.authorityOverrideFunction = Objects.requireNonNull(authorityOverrideFunction);
    }

    @SuppressWarnings({"ReactiveStreamsUnusedPublisher"})
    public Mono<HttpResponse> send(final HttpRequest azureRequest) {
        try {
            final var apacheRequest = new ApacheHttpRequest(azureRequest.getHttpMethod(),
                    azureRequest.getUrl(), azureRequest.getHeaders(), authorityOverrideFunction);

            final Mono<byte[]> bodyMono;
            if (azureRequest.getBody() != null) {
                bodyMono = FluxUtil.collectBytesInByteBufferStream(azureRequest.getBody());
            } else {
                bodyMono = Mono.just(new byte[0]);
            }

            return bodyMono.flatMap(bodyBytes -> {
                apacheRequest.setEntity(new ByteArrayEntity(bodyBytes));
                try {
                    final var response = httpClient.execute(apacheRequest);
                    return Mono.just(new ApacheHttpResponse(azureRequest, response));
                } catch (final IOException ex) {
                    return Mono.error(ex);
                }
            });
        } catch (final URISyntaxException e) {
            return Mono.error(e);
        }
    }

    private static CloseableHttpClient createHttpClient(
            final TrustStrategy trustStrategy,
            final HostnameVerifier hostnameVerifier) {
        try {
            final var builder = new SSLContextBuilder();
            builder.loadTrustMaterial(null, trustStrategy);
            final var socketFactory = new SSLConnectionSocketFactory(builder.build(), hostnameVerifier);
            return HttpClients.custom()
                    .addInterceptorFirst(new ContentLengthHeaderRemover())
                    .setSSLSocketFactory(socketFactory).build();
        } catch (final Exception e) {
            throw new LowkeyVaultException("Failed to create HTTP client.", e);
        }
    }

}
