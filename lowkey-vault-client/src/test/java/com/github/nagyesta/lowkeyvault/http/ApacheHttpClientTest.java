package com.github.nagyesta.lowkeyvault.http;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ApacheHttpClientTest {

    private static final String HEADER_1 = "Header1";
    private static final String HEADER_VALUE_1 = "HeaderValue1";
    private static final String HEADER_2 = "Header2";
    private static final String HEADER_VALUE_2 = "HeaderValue2";
    private static final String LOCALHOST = "localhost";
    private static final String ALTERNATIVE_HOST = "alternative.example.com";
    private static final Function<URI, URI> LOCALHOST_TO_ALTERNATIVE = new AuthorityOverrideFunction(LOCALHOST, ALTERNATIVE_HOST);
    private static final String EMPTY = "";

    public static Stream<Arguments> validProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(null, Function.identity(), LOCALHOST))
                .add(Arguments.of(EMPTY, Function.identity(), LOCALHOST))
                .add(Arguments.of(null, LOCALHOST_TO_ALTERNATIVE, ALTERNATIVE_HOST))
                .add(Arguments.of(EMPTY, LOCALHOST_TO_ALTERNATIVE, ALTERNATIVE_HOST))
                .build();
    }

    @ParameterizedTest
    @MethodSource("validProvider")
    void testConstructorShouldConvertValuesWhenCalled(
            final String body, final Function<URI, URI> hostOverrideFunction, final String expectedHost) throws IOException {
        //given
        final HttpMethod method = HttpMethod.POST;
        final URL url = new URL("https://localhost");
        final HttpHeaders headers = new HttpHeaders(Map.of(HEADER_1, HEADER_VALUE_1, HEADER_2, HEADER_VALUE_2));
        final HttpClient client = mock(HttpClient.class);
        final HttpRequest azureRequest;
        if (body != null) {
            azureRequest = new HttpRequest(method, url, headers,
                    Flux.defer(() -> Flux.just(ByteBuffer.wrap(body.getBytes(StandardCharsets.UTF_8)))));
        } else {
            azureRequest = new HttpRequest(method, url, headers, null);
        }
        final org.apache.http.HttpResponse response = ApacheHttpResponseTest.responseMock();
        final ArgumentCaptor<HttpUriRequest> captor = ArgumentCaptor.forClass(HttpUriRequest.class);
        when(client.execute(captor.capture())).thenReturn(response);
        final ApacheHttpClient underTest = new ApacheHttpClient(client, hostOverrideFunction);

        //when
        final HttpResponse actual = underTest.send(azureRequest).block();

        //then
        Assertions.assertNotNull(actual);
        ApacheHttpResponseTest.verifyResponse((ApacheHttpResponse) actual);
        Assertions.assertEquals(expectedHost, captor.getValue().getURI().getHost());
    }
}
