package com.github.nagyesta.lowkeyvault.controller.common;

import com.github.nagyesta.lowkeyvault.controller.AuthTokenGenerator;
import com.github.nagyesta.lowkeyvault.controller.MetadataController;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

class MetadataControllerTest {

    private static final String KEY_STORE_PASSWORD = "changeit";
    private static final String KEY_STORE_RESOURCE = "cert/keystore.p12";
    private static final String REALM_NAME = "realm-name";
    private static final String TOKEN_ISSUER = "https://token-issuer.example.com/";
    private static final AuthTokenGenerator GENERATOR = new AuthTokenGenerator(TOKEN_ISSUER);

    public static Stream<Arguments> nullProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(null, null, null, null))
                .add(Arguments.of(REALM_NAME, null, null, null))
                .add(Arguments.of(null, GENERATOR, null, null))
                .add(Arguments.of(null, null, KEY_STORE_RESOURCE, null))
                .add(Arguments.of(null, null, null, KEY_STORE_PASSWORD))
                .add(Arguments.of(null, GENERATOR, KEY_STORE_RESOURCE, KEY_STORE_PASSWORD))
                .add(Arguments.of(REALM_NAME, GENERATOR, null, KEY_STORE_PASSWORD))
                .add(Arguments.of(REALM_NAME, null, KEY_STORE_RESOURCE, KEY_STORE_PASSWORD))
                .add(Arguments.of(REALM_NAME, GENERATOR, KEY_STORE_RESOURCE, null))
                .build();
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    public static Stream<Arguments> openIdConfigProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of("http", "localhost", 80,
                        URI.create("http://localhost/metadata/identity/.well-known/openid-configuration/jwks")))
                .add(Arguments.of("http", "localhost", 81,
                        URI.create("http://localhost:81/metadata/identity/.well-known/openid-configuration/jwks")))
                .add(Arguments.of("http", "example.com", 8080,
                        URI.create("http://example.com:8080/metadata/identity/.well-known/openid-configuration/jwks")))
                .build();
    }

    @Test
    void testGetManagedIdentityTokenShouldReturnTokenWhenCalled() throws IOException {
        //given
        final var underTest = new MetadataController(REALM_NAME, GENERATOR, KEY_STORE_RESOURCE, KEY_STORE_PASSWORD);
        final var resource = URI.create("https://localhost:8443/");

        //when
        final var actual = underTest.getManagedIdentityToken(resource);

        //then
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(HttpStatus.OK, actual.getStatusCode());
        Assertions.assertNotNull(actual.getBody());
        Assertions.assertEquals(resource, actual.getBody().resource());
        Assertions.assertNotNull(actual.getBody().accessToken());
        final var claims = (Claims) Jwts.parser().verifyWith(GENERATOR.getKeyPair().getPublic()).build()
                .parse(actual.getBody().accessToken()).getPayload();
        Assertions.assertEquals(TOKEN_ISSUER, claims.getIssuer());
        Assertions.assertEquals(Set.of(resource.toString()), claims.getAudience());
        Assertions.assertNotNull(claims.getExpiration());
        Assertions.assertNotNull(claims.getNotBefore());
        Assertions.assertNotNull(claims.getIssuedAt());
        Assertions.assertEquals(List.of("Basic realm=" + REALM_NAME), actual.getHeaders().get(HttpHeaders.WWW_AUTHENTICATE));
    }

    @ParameterizedTest
    @MethodSource("openIdConfigProvider")
    void testGetOpenIdConfigurationShouldReturnConfigurationWhenCalled(
            final String scheme,
            final String host,
            final int port,
            final URI expectedUri) throws IOException {
        //given
        final var underTest = new MetadataController(REALM_NAME, GENERATOR, KEY_STORE_RESOURCE, KEY_STORE_PASSWORD);
        final var request = new MockHttpServletRequest();
        request.setScheme(scheme);
        request.setServerName(host);
        request.setServerPort(port);

        //when
        final var actual = underTest.getOpenIdConfiguration(request);

        //then
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(HttpStatus.OK, actual.getStatusCode());
        Assertions.assertNotNull(actual.getBody());
        Assertions.assertEquals(TOKEN_ISSUER, actual.getBody().issuer());
        Assertions.assertEquals(expectedUri, actual.getBody().jwksUri());
    }

    @Test
    void testGetJwksShouldReturnKeyDetails() throws IOException {
        //given
        final var underTest = new MetadataController(REALM_NAME, GENERATOR, KEY_STORE_RESOURCE, KEY_STORE_PASSWORD);

        //when
        final var actual = underTest.getJwks();

        //then
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(HttpStatus.OK, actual.getStatusCode());
        Assertions.assertNotNull(actual.getBody());
        final var actualKeys = actual.getBody().keys();
        Assertions.assertNotNull(actualKeys);
        Assertions.assertEquals(1, actualKeys.size());
        final var keyModel = actualKeys.getFirst();
        Assertions.assertNotNull(keyModel);
        Assertions.assertNotNull(keyModel.getN());
        Assertions.assertNotNull(keyModel.getE());
        Assertions.assertNotNull(keyModel.getD());
        Assertions.assertNotNull(keyModel.getDp());
        Assertions.assertNotNull(keyModel.getDq());
        Assertions.assertNotNull(keyModel.getP());
        Assertions.assertNotNull(keyModel.getQ());
        Assertions.assertNotNull(keyModel.getQi());
        Assertions.assertNull(keyModel.getKeyType());
        Assertions.assertNull(keyModel.getKeyHsm());
        Assertions.assertNull(keyModel.getKeyOps());
        Assertions.assertNull(keyModel.getCurveName());
        Assertions.assertNull(keyModel.getX());
        Assertions.assertNull(keyModel.getY());

    }

    @Test
    void testGetDefaultCertificateStoreContentShouldReturnResourceContents() throws IOException {
        //given
        final var underTest = new MetadataController(REALM_NAME, GENERATOR, KEY_STORE_RESOURCE, KEY_STORE_PASSWORD);
        final var expected = getResourceContent();

        //when
        final var actual = underTest.getDefaultCertificateStoreContent();

        //then
        Assertions.assertNotNull(actual);
        Assertions.assertArrayEquals(expected, actual);
    }

    @Test
    void testGetDefaultCertificateStorePasswordShouldReturnPassword() throws IOException {
        //given
        final var underTest = new MetadataController(REALM_NAME, GENERATOR, KEY_STORE_RESOURCE, KEY_STORE_PASSWORD);

        //when
        final var actual = underTest.getDefaultCertificateStorePassword();

        //then
        Assertions.assertEquals(KEY_STORE_PASSWORD, actual);
    }

    @ParameterizedTest
    @MethodSource("nullProvider")
    void testConstructorShouldThrowExceptionWhenCalledWithNull(
            final String realm,
            final AuthTokenGenerator generator,
            final String resource,
            final String password) {
        //given

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> new MetadataController(realm, generator, resource, password));

        //then + exception
    }

    private byte[] getResourceContent() throws IOException {
        final var url = getClass().getResource("/" + KEY_STORE_RESOURCE);
        if (url == null) {
            throw new IOException("Resource not found: " + KEY_STORE_RESOURCE);
        }
        try (var inputStream = url.openStream()) {
            return inputStream.readAllBytes();
        }
    }
}
