package com.github.nagyesta.lowkeyvault.http;

import com.azure.core.http.*;
import com.azure.core.util.FluxUtil;
import org.apache.http.HttpException;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLContextBuilder;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

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

    public Mono<HttpResponse> send(final HttpRequest azureRequest) {
        try {
            final ApacheHttpRequest apacheRequest = new ApacheHttpRequest(azureRequest.getHttpMethod(), azureRequest.getUrl(),
                    azureRequest.getHeaders());

            final Mono<byte[]> bodyMono = (azureRequest.getBody() != null)
                    ? FluxUtil.collectBytesInByteBufferStream(azureRequest.getBody())
                    : Mono.just(new byte[0]);

            return bodyMono.flatMap(bodyBytes -> {
                apacheRequest.setEntity(new ByteArrayEntity(bodyBytes));
                try {
                    @SuppressWarnings("BlockingMethodInNonBlockingContext") final org.apache.http.HttpResponse response = httpClient.execute(apacheRequest);
                    return Mono.just(new ApacheHttpResponse(azureRequest, response));
                } catch (final IOException ex) {
                    return Mono.error(ex);
                }
            });
        } catch (final URISyntaxException e) {
            return Mono.error(e);
        }
    }

    private static class ContentLengthHeaderRemover implements HttpRequestInterceptor {
        @Override
        public void process(final org.apache.http.HttpRequest request, final HttpContext context) throws HttpException, IOException {
            request.removeHeaders(HTTP.CONTENT_LEN);
            // fighting org.apache.http.protocol.RequestContent's ProtocolException("Content-Length header already present");
        }
    }

    private static final class ApacheHttpRequest extends HttpEntityEnclosingRequestBase {
        private final String method;

        private ApacheHttpRequest(final HttpMethod method, final URL url, final HttpHeaders headers) throws URISyntaxException {
            this.method = method.name();
            setURI(url.toURI());
            headers.stream().forEach(header -> addHeader(header.getName(), header.getValue()));
        }

        @Override
        public String getMethod() {
            return method;
        }
    }
}
