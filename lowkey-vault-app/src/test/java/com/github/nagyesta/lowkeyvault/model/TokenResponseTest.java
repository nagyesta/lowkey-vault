package com.github.nagyesta.lowkeyvault.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.net.URI;
import java.time.Instant;
import java.util.stream.Stream;

class TokenResponseTest {

    private static final int EXPECTED_EXPIRY = 48 * 3600;
    private static final Instant NOW = Instant.now();
    private static final Instant EXPIRES_ON = NOW.plusSeconds(EXPECTED_EXPIRY);
    private static final long MIN_EXPIRES_ON = EXPIRES_ON.getEpochSecond();
    private static final String TOKEN_TYPE = "Bearer";
    private static final String DUMMY_TOKEN = "dummy";
    private static final URI RESOURCE = URI.create("https://localhost:8443/path");

    public static Stream<Arguments> nullValuesProvider() {
        final var resource = RESOURCE;
        final var token = DUMMY_TOKEN;
        final long expiresIn = EXPECTED_EXPIRY;
        final var expiresOn = MIN_EXPIRES_ON;
        final var tokenType = TOKEN_TYPE;

        return Stream.of(
                Arguments.of(null, token, token, expiresIn, expiresOn, tokenType),
                Arguments.of(resource, null, token, expiresIn, expiresOn, tokenType),
                Arguments.of(resource, token, null, expiresIn, expiresOn, tokenType)
        );
    }

    public static Stream<Arguments> nullAndEmptyProvider() {
        final var resource = RESOURCE;
        final var token = DUMMY_TOKEN;
        final var now = NOW;
        final var expiresOn = EXPIRES_ON;
        return Stream.<Arguments>builder()
                .add(Arguments.of(null, token, now, expiresOn))
                .add(Arguments.of(URI.create(""), token, now, expiresOn))
                .add(Arguments.of(resource, null, now, expiresOn))
                .add(Arguments.of(resource, token, null, expiresOn))
                .add(Arguments.of(resource, token, now, null))
                .build();
    }

    @ParameterizedTest
    @MethodSource("nullAndEmptyProvider")
    void testConstructorShouldThrowExceptionWhenCalledWithNullOrEmpty(
            final URI resource,
            final String token,
            final Instant issuedAt,
            final Instant expiresOn) {
        //given

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> new TokenResponse(resource, token, issuedAt, expiresOn));

        //then + expected
    }

    @ParameterizedTest
    @MethodSource("nullValuesProvider")
    void testConstructorShouldThrowExceptionWhenCalledWithNulls(
            final URI resource, final String accessToken, final String refreshToken,
            final long expiresIn, final long expiresOn, final String tokenType) {
        //given

        //when
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new TokenResponse(resource, accessToken, refreshToken, expiresIn, expiresOn, tokenType));

        //then + expected
    }

    @Test
    void testConstructorShouldReturnNonExpiredTokenValidForTheProvidedResourceWhenCalledWithValidResource() {
        //given

        //when
        final var actual = new TokenResponse(RESOURCE, DUMMY_TOKEN, NOW, EXPIRES_ON);

        //then
        Assertions.assertEquals(RESOURCE, actual.resource());
        Assertions.assertEquals(DUMMY_TOKEN, actual.accessToken());
        Assertions.assertEquals(DUMMY_TOKEN, actual.refreshToken());
        Assertions.assertEquals(EXPECTED_EXPIRY, actual.expiresIn());
        Assertions.assertTrue(MIN_EXPIRES_ON <= actual.expiresOn());
        Assertions.assertEquals(TOKEN_TYPE, actual.tokenType());
    }
}
