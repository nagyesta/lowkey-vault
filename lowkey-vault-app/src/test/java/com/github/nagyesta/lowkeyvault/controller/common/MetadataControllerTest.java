package com.github.nagyesta.lowkeyvault.controller.common;

import com.github.nagyesta.lowkeyvault.controller.MetadataController;
import com.github.nagyesta.lowkeyvault.model.TokenResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.stream.Stream;

class MetadataControllerTest {

    private static final String KEY_STORE_PASSWORD = "changeit";
    private static final String KEY_STORE_RESOURCE = "cert/keystore.p12";
    private static final String REALM_NAME = "realm-name";

    public static Stream<Arguments> nullProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(null, null, null))
                .add(Arguments.of(REALM_NAME, null, null))
                .add(Arguments.of(null, KEY_STORE_RESOURCE, null))
                .add(Arguments.of(null, null, KEY_STORE_PASSWORD))
                .add(Arguments.of(null, KEY_STORE_RESOURCE, KEY_STORE_PASSWORD))
                .add(Arguments.of(REALM_NAME, null, KEY_STORE_PASSWORD))
                .add(Arguments.of(REALM_NAME, KEY_STORE_RESOURCE, null))
                .build();
    }

    @Test
    void testGetManagedIdentityTokenShouldReturnTokenWhenCalled() throws IOException {
        //given
        final MetadataController underTest = new MetadataController(REALM_NAME, KEY_STORE_RESOURCE, KEY_STORE_PASSWORD);
        final URI resource = URI.create("https://localhost:8443/");

        //when
        final ResponseEntity<TokenResponse> actual = underTest.getManagedIdentityToken(resource);

        //then
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(HttpStatus.OK, actual.getStatusCode());
        Assertions.assertNotNull(actual.getBody());
        Assertions.assertEquals(resource, actual.getBody().resource());
        Assertions.assertEquals("dummy", actual.getBody().accessToken());
        Assertions.assertEquals(List.of("Basic realm=" + REALM_NAME), actual.getHeaders().get(HttpHeaders.WWW_AUTHENTICATE));
    }

    @Test
    void testGetDefaultCertificateStoreContentShouldReturnResourceContents() throws IOException {
        //given
        final MetadataController underTest = new MetadataController(REALM_NAME, KEY_STORE_RESOURCE, KEY_STORE_PASSWORD);
        final byte[] expected = getResourceContent();

        //when
        final byte[] actual = underTest.getDefaultCertificateStoreContent();

        //then
        Assertions.assertNotNull(actual);
        Assertions.assertArrayEquals(expected, actual);
    }

    @Test
    void testGetDefaultCertificateStorePasswordShouldReturnPassword() throws IOException {
        //given
        final MetadataController underTest = new MetadataController(REALM_NAME, KEY_STORE_RESOURCE, KEY_STORE_PASSWORD);

        //when
        final String actual = underTest.getDefaultCertificateStorePassword();

        //then
        Assertions.assertEquals(KEY_STORE_PASSWORD, actual);
    }

    @ParameterizedTest
    @MethodSource("nullProvider")
    void testConstructorShouldThrowExceptionWhenCalledWithNull(final String realm, final String resource, final String password) {
        //given

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> new MetadataController(realm, resource, password));

        //then + exception
    }

    private byte[] getResourceContent() throws IOException {
        final URL url = getClass().getResource("/" + KEY_STORE_RESOURCE);
        if (url == null) {
            throw new IOException("Resource not found: " + KEY_STORE_RESOURCE);
        }
        //noinspection LocalCanBeFinal
        try (InputStream inputStream = url.openStream()) {
            return inputStream.readAllBytes();
        }
    }
}
