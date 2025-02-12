package com.github.nagyesta.lowkeyvault.http.management.impl;

import com.azure.core.http.HttpResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.github.nagyesta.lowkeyvault.http.management.LowkeyVaultException;
import com.github.nagyesta.lowkeyvault.http.management.VaultModel;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.github.nagyesta.lowkeyvault.http.management.impl.ResponseEntity.VAULT_MODEL_LIST_TYPE_REF;
import static org.mockito.Mockito.*;

class ResponseEntityTest {

    private static final String HTTPS_DEFAULT_LOCALHOST_8443 = "https://default.localhost:8443";
    private static final String VAULT_MODEL = "{\"baseUri\":\"" + HTTPS_DEFAULT_LOCALHOST_8443 + "\"}";
    private static final String VAULT_MODEL_LIST = "[{\"baseUri\":\"" + HTTPS_DEFAULT_LOCALHOST_8443 + "\"}]";

    @SuppressWarnings("checkstyle:MagicNumber")
    public static Stream<Arguments> responseCodeProvider() {
        final Stream<Arguments> negative = IntStream.of(1, 100, 199, 300, 404)
                .mapToObj(i -> Arguments.of(i, false));
        final Stream<Arguments> positive = IntStream.of(200, 201, 204, 299)
                .mapToObj(i -> Arguments.of(i, true));
        return Stream.of(negative, positive).flatMap(Function.identity());
    }

    public static Stream<Arguments> nullProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(null, null))
                .add(Arguments.of(mock(HttpResponse.class), null))
                .add(Arguments.of(null, mock(ObjectReader.class)))
                .build();
    }

    @ParameterizedTest
    @MethodSource("nullProvider")
    void testConstructorShouldThrowExceptionWhenCalledWithNulls(final HttpResponse response, final ObjectReader reader) {
        //given

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> new ResponseEntity(response, reader));

        //then + exception
    }

    @ParameterizedTest
    @MethodSource("responseCodeProvider")
    void testIsSuccessfulShouldReturnTrueWhenResponseCodeIs2xx(final int code, final boolean expected) {
        //given
        final HttpResponse response = mock(HttpResponse.class);
        when(response.getStatusCode()).thenReturn(code);
        when(response.getBodyAsString(eq(StandardCharsets.UTF_8))).thenReturn(Mono.empty());
        final ResponseEntity underTest = new ResponseEntity(response, mock(ObjectReader.class));

        //when
        final boolean actual = underTest.isSuccessful();

        //then
        Assertions.assertEquals(expected, actual);
        verify(response).getStatusCode();
        verify(response).getBodyAsString(eq(StandardCharsets.UTF_8));
    }

    @ParameterizedTest
    @MethodSource("responseCodeProvider")
    void testGetResponseCodeShouldReturnTheCodeUnchangedWhenCalled(final int code) {
        //given
        final HttpResponse response = mock(HttpResponse.class);
        when(response.getStatusCode()).thenReturn(code);
        when(response.getBodyAsString(eq(StandardCharsets.UTF_8))).thenReturn(Mono.empty());
        final ResponseEntity underTest = new ResponseEntity(response, mock(ObjectReader.class));

        //when
        final int actual = underTest.getResponseCode();

        //then
        Assertions.assertEquals(code, actual);
        verify(response).getStatusCode();
        verify(response).getBodyAsString(eq(StandardCharsets.UTF_8));
    }

    @Test
    void testGetResponseObjectShouldMapResponseBodyUsingObjectReaderWhenCalledWithSingleObject() {
        //given
        final VaultModel expected = new VaultModel(URI.create(HTTPS_DEFAULT_LOCALHOST_8443), null, null, null, null, null);
        final HttpResponse response = mock(HttpResponse.class);
        when(response.getStatusCode()).thenReturn(HttpStatus.SC_OK);
        when(response.getBodyAsString(eq(StandardCharsets.UTF_8))).thenReturn(Mono.just(VAULT_MODEL));
        final ObjectReader reader = new ObjectMapper().reader();
        final ResponseEntity underTest = new ResponseEntity(response, reader);

        //when
        final VaultModel actual = underTest.getResponseObject(VaultModel.class);

        //then
        Assertions.assertEquals(expected, actual);
        verify(response).getStatusCode();
        verify(response).getBodyAsString(eq(StandardCharsets.UTF_8));
    }

    @Test
    void testGetResponseObjectShouldMapResponseBodyUsingObjectReaderWhenCalledWithList() {
        //given
        final VaultModel vaultModel = new VaultModel(URI.create(HTTPS_DEFAULT_LOCALHOST_8443), null, null, null, null, null);
        final List<VaultModel> expected = Collections.singletonList(vaultModel);
        final HttpResponse response = mock(HttpResponse.class);
        when(response.getStatusCode()).thenReturn(HttpStatus.SC_OK);
        when(response.getBodyAsString(eq(StandardCharsets.UTF_8))).thenReturn(Mono.just(VAULT_MODEL_LIST));
        final ObjectReader reader = new ObjectMapper().reader();
        final ResponseEntity underTest = new ResponseEntity(response, reader);

        //when
        final List<VaultModel> actual = underTest.getResponseObject(VAULT_MODEL_LIST_TYPE_REF);

        //then
        Assertions.assertEquals(expected, actual);
        verify(response).getStatusCode();
        verify(response).getBodyAsString(eq(StandardCharsets.UTF_8));
    }

    @Test
    void testGetResponseObjectShouldThrowExceptionWhenJsonProcessingExceptionIsThrownByReaderWhileMappingToSingleObject()
            throws JsonProcessingException {
        //given
        final HttpResponse response = mock(HttpResponse.class);
        when(response.getStatusCode()).thenReturn(HttpStatus.SC_OK);
        when(response.getBodyAsString(eq(StandardCharsets.UTF_8))).thenReturn(Mono.just(VAULT_MODEL));
        final ObjectReader reader = mock(ObjectReader.class);
        when(reader.forType(eq(VaultModel.class))).thenReturn(reader);
        when(reader.readValue(anyString())).thenThrow(JsonProcessingException.class);
        final ResponseEntity underTest = new ResponseEntity(response, reader);

        //when
        Assertions.assertThrows(LowkeyVaultException.class, () -> underTest.getResponseObject(VaultModel.class));

        //then + exception
    }

    @Test
    void testGetResponseObjectShouldThrowExceptionWhenJsonProcessingExceptionIsThrownByReaderWhileMappingToList()
            throws JsonProcessingException {
        //given
        final HttpResponse response = mock(HttpResponse.class);
        when(response.getStatusCode()).thenReturn(HttpStatus.SC_OK);
        when(response.getBodyAsString(eq(StandardCharsets.UTF_8))).thenReturn(Mono.just(VAULT_MODEL_LIST));
        final ObjectReader reader = mock(ObjectReader.class);
        when(reader.forType(eq(VAULT_MODEL_LIST_TYPE_REF))).thenReturn(reader);
        when(reader.readValue(anyString())).thenThrow(JsonProcessingException.class);
        final ResponseEntity underTest = new ResponseEntity(response, reader);

        //when
        Assertions.assertThrows(LowkeyVaultException.class, () -> underTest.getResponseObject(VAULT_MODEL_LIST_TYPE_REF));

        //then + exception
    }
}
