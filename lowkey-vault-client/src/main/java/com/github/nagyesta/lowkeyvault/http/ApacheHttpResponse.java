package com.github.nagyesta.lowkeyvault.http;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import org.apache.http.HttpEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Optional;

/**
 * Modified class based on https://github.com/Azure/azure-sdk-for-java/wiki/Custom-HTTP-Clients.
 */
final class ApacheHttpResponse extends HttpResponse {
    private final int statusCode;
    private final HttpHeaders headers;
    private final String entity;

    ApacheHttpResponse(final HttpRequest request, final org.apache.http.HttpResponse apacheResponse) throws IOException {
        super(request);
        this.statusCode = apacheResponse.getStatusLine().getStatusCode();
        this.headers = new HttpHeaders();
        Arrays.stream(apacheResponse.getAllHeaders())
                .forEach(header -> headers.set(header.getName(), header.getValue()));
        final HttpEntity responseEntity = Optional.ofNullable(apacheResponse.getEntity()).orElse(new StringEntity(""));
        this.entity = EntityUtils.toString(responseEntity, StandardCharsets.UTF_8);
    }

    @Override
    public int getStatusCode() {
        return statusCode;
    }

    @Override
    public String getHeaderValue(final String s) {
        return getHeaders().getValue(s);
    }

    @Override
    public HttpHeaders getHeaders() {
        return headers;
    }

    @Override
    public Flux<ByteBuffer> getBody() {
        return getBodyAsByteArray().map(ByteBuffer::wrap).flux();
    }

    @Override
    public Mono<byte[]> getBodyAsByteArray() {
        return Mono.just(entity.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Mono<String> getBodyAsString() {
        return getBodyAsString(StandardCharsets.UTF_8);
    }

    @Override
    public Mono<String> getBodyAsString(final Charset charset) {
        return getBodyAsByteArray().map(bytes -> new String(bytes, charset));
    }
}
