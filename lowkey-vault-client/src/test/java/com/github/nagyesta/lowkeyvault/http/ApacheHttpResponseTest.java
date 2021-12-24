package com.github.nagyesta.lowkeyvault.http;

import com.azure.core.http.HttpRequest;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ApacheHttpResponseTest {

    private static final String BODY = "body";
    private static final int STATUS_CODE = 200;
    private static final String HEADER_1 = "Header1";
    private static final String HEADER_VALUE_1 = "HeaderValue1";
    private static final String HEADER_2 = "Header2";
    private static final String HEADER_VALUE_2 = "HeaderValue2";

    static void verifyResponse(final ApacheHttpResponse actual) {
        Assertions.assertEquals(BODY, actual.getBodyAsString().block());
        Assertions.assertEquals(STATUS_CODE, actual.getStatusCode());
        Assertions.assertEquals(HEADER_VALUE_1, actual.getHeaderValue(HEADER_1));
        Assertions.assertEquals(HEADER_VALUE_2, actual.getHeaderValue(HEADER_2));
    }

    static HttpResponse responseMock() throws IOException {
        final HttpResponse response = mock(HttpResponse.class);

        final HttpEntity httpEntity = mock(HttpEntity.class);
        when(response.getEntity()).thenReturn(httpEntity);
        when(httpEntity.getContent()).thenReturn(new ByteArrayInputStream(BODY.getBytes(StandardCharsets.UTF_8)));

        final StatusLine statusLine = mock(StatusLine.class);
        when(response.getStatusLine()).thenReturn(statusLine);
        when(statusLine.getStatusCode()).thenReturn(STATUS_CODE);

        final Header header1 = mock(Header.class);
        when(header1.getName()).thenReturn(HEADER_1);
        when(header1.getValue()).thenReturn(HEADER_VALUE_1);
        final Header header2 = mock(Header.class);
        when(header2.getName()).thenReturn(HEADER_2);
        when(header2.getValue()).thenReturn(HEADER_VALUE_2);
        when(response.getAllHeaders()).thenReturn(new Header[]{header1, header2});
        return response;
    }

    @Test
    void testConstructorShouldMapFieldsWhenCalled() throws IOException {
        //given
        final HttpRequest request = mock(HttpRequest.class);
        final HttpResponse response = responseMock();

        //when
        final ApacheHttpResponse actual = new ApacheHttpResponse(request, response);

        //then
        verifyResponse(actual);
    }
}
