package com.github.nagyesta.lowkeyvault.http;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.FluxUtil;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;
import java.util.function.Function;

public final class ApacheHttpClient implements HttpClient {
    private final org.apache.http.client.HttpClient httpClient;
    private final Function<URI, URI> authorityOverrideFunction;

    public ApacheHttpClient(final Function<URI, URI> authorityOverrideFunction) {
        try {
            this.authorityOverrideFunction = Objects.requireNonNull(authorityOverrideFunction);
            final SSLContextBuilder builder = new SSLContextBuilder();
            builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
            final SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(builder.build(), new DefaultHostnameVerifier());
            this.httpClient = HttpClients.custom()
                    .addInterceptorFirst(new ContentLengthHeaderRemover())
                    .setSSLSocketFactory(socketFactory).build();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    ApacheHttpClient(final org.apache.http.client.HttpClient httpClient, final Function<URI, URI> authorityOverrideFunction) {
        this.httpClient = Objects.requireNonNull(httpClient);
        this.authorityOverrideFunction = Objects.requireNonNull(authorityOverrideFunction);
    }

    @SuppressWarnings("BlockingMethodInNonBlockingContext")
    public Mono<HttpResponse> send(final HttpRequest azureRequest) {
        try {
            final ApacheHttpRequest apacheRequest = new ApacheHttpRequest(azureRequest.getHttpMethod(),
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
                    final org.apache.http.HttpResponse response = httpClient.execute(apacheRequest);
                    return Mono.just(new ApacheHttpResponse(azureRequest, response));
                } catch (final IOException ex) {
                    return Mono.error(ex);
                }
            });
        } catch (final URISyntaxException e) {
            return Mono.error(e);
        }
    }

}
