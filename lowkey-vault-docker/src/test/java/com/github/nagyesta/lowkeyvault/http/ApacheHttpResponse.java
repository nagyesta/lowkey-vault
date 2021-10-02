package com.github.nagyesta.lowkeyvault.http;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

final class ApacheHttpResponse extends HttpResponse {
    private final int statusCode;
    private final HttpHeaders headers;
    private final String entity;

    ApacheHttpResponse(final HttpRequest request, final org.apache.http.HttpResponse apacheResponse) throws IOException {
        super(request);
        this.statusCode = apacheResponse.getStatusLine().getStatusCode();
        this.headers = new HttpHeaders();
        Arrays.stream(apacheResponse.getAllHeaders())
                .forEach(header -> headers.put(header.getName(), header.getValue()));
        this.entity = EntityUtils.toString(apacheResponse.getEntity(), StandardCharsets.UTF_8);
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getHeaderValue(final String s) {
        return headers.getValue(s);
    }

    public HttpHeaders getHeaders() {
        return headers;
    }

    public Flux<ByteBuffer> getBody() {
        return getBodyAsByteArray().map(ByteBuffer::wrap).flux();
    }

    public Mono<byte[]> getBodyAsByteArray() {
        return Mono.just(entity.getBytes(StandardCharsets.UTF_8));
    }

    public Mono<String> getBodyAsString() {
        return getBodyAsByteArray().map(String::new);
    }

    public Mono<String> getBodyAsString(final Charset charset) {
        return getBodyAsByteArray().map(bytes -> new String(bytes, charset));
    }
}
