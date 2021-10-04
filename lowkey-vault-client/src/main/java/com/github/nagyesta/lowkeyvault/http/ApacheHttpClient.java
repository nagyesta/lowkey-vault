package com.github.nagyesta.lowkeyvault.http;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.FluxUtil;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.URISyntaxException;

public final class ApacheHttpClient implements HttpClient {
    private final org.apache.http.client.HttpClient httpClient;

    public ApacheHttpClient() {
        try {
            final SSLContextBuilder builder = new SSLContextBuilder();
            builder.loadTrustMaterial(null, (chain, authType) -> true);
            final SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(builder.build(), new NoopHostnameVerifier());
            this.httpClient = HttpClients.custom()
                    .addInterceptorFirst(new ContentLengthHeaderRemover())
                    .setSSLSocketFactory(socketFactory).build();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    public ApacheHttpClient(final org.apache.http.client.HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @SuppressWarnings("BlockingMethodInNonBlockingContext")
    public Mono<HttpResponse> send(final HttpRequest azureRequest) {
        try {
            final ApacheHttpRequest apacheRequest = new ApacheHttpRequest(azureRequest.getHttpMethod(),
                    azureRequest.getUrl(), azureRequest.getHeaders());

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
