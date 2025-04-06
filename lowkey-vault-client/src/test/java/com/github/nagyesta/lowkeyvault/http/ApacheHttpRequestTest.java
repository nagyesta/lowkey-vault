package com.github.nagyesta.lowkeyvault.http;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;

class ApacheHttpRequestTest {

    private static final String HEADER_1 = "Header1";
    private static final String HEADER_VALUE_1 = "HeaderValue1";
    private static final String HEADER_2 = "Header2";
    private static final String HEADER_VALUE_2 = "HeaderValue2";

    @Test
    void testConstructorShouldConvertValuesWhenCalled() throws MalformedURLException, URISyntaxException {
        //given
        final var method = HttpMethod.POST;
        final var url = new URL("https://localhost");
        final var headers = new HttpHeaders(Map.of(HEADER_1, HEADER_VALUE_1, HEADER_2, HEADER_VALUE_2));

        //when
        final var actual = new ApacheHttpRequest(method, url, headers, uri -> uri);

        //then
        Assertions.assertEquals(method.toString(), actual.getMethod());
        Assertions.assertEquals(url.toURI(), actual.getURI());
        Assertions.assertEquals(HEADER_VALUE_1, actual.getHeaders(HEADER_1)[0].getValue());
        Assertions.assertEquals(HEADER_VALUE_2, actual.getHeaders(HEADER_2)[0].getValue());
    }
}
