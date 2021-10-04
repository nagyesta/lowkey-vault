package com.github.nagyesta.lowkeyvault.http;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ApacheHttpClientTest {

    private static final String HEADER_1 = "Header1";
    private static final String HEADER_VALUE_1 = "HeaderValue1";
    private static final String HEADER_2 = "Header2";
    private static final String HEADER_VALUE_2 = "HeaderValue2";

    @Test
    void testConstructorShouldConvertValuesWhenCalled() throws IOException, URISyntaxException {
        //given
        final HttpMethod method = HttpMethod.POST;
        final URL url = new URL("https://localhost");
        final HttpHeaders headers = new HttpHeaders(Map.of(HEADER_1, HEADER_VALUE_1, HEADER_2, HEADER_VALUE_2));
        final HttpClient client = mock(HttpClient.class);
        final HttpRequest azureRequest = new HttpRequest(method, url, headers, null);
        final org.apache.http.HttpResponse response = ApacheHttpResponseTest.responseMock();
        when(client.execute(any())).thenReturn(response);
        final ApacheHttpClient underTest = new ApacheHttpClient(client);

        //when
        final HttpResponse actual = underTest.send(azureRequest).block();

        //then
        Assertions.assertNotNull(actual);
        ApacheHttpResponseTest.verifyResponse((ApacheHttpResponse) actual);
    }
}
