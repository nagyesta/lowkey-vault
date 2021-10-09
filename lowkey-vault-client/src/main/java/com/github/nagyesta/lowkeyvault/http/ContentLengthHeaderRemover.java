package com.github.nagyesta.lowkeyvault.http;

import org.apache.http.HttpRequestInterceptor;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;

class ContentLengthHeaderRemover implements HttpRequestInterceptor {
    @Override
    public void process(final org.apache.http.HttpRequest request, final HttpContext context) {
        request.removeHeaders(HTTP.CONTENT_LEN);
        // fighting org.apache.http.protocol.RequestContent's ProtocolException("Content-Length header already present");
    }
}
