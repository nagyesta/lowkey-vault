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
    private static final long MIN_EXPIRES_ON = Instant.now().plusSeconds(EXPECTED_EXPIRY).getEpochSecond();
    private static final int TOKEN_TYPE = 1;
    private static final String DUMMY_TOKEN = "dummy";
    private static final URI RESOURCE = URI.create("https://localhost:8443/path");

    public static Stream<Arguments> nullValuesProvider() {
        final URI resource = RESOURCE;
        final String token = DUMMY_TOKEN;
        final long expiresIn = EXPECTED_EXPIRY;
        final long expiresOn = MIN_EXPIRES_ON;
        final int tokenType = TOKEN_TYPE;

        return Stream.of(
                Arguments.of(null, token, token, expiresIn, expiresOn, tokenType),
                Arguments.of(resource, null, token, expiresIn, expiresOn, tokenType),
                Arguments.of(resource, token, null, expiresIn, expiresOn, tokenType)
        );
    }

    @Test
    void testConstructorShouldThrowExceptionWhenCalledWithNull() {
        //given

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> new TokenResponse(null));

        //then + expected
    }

    @Test
    void testConstructorShouldThrowExceptionWhenCalledWithEmptyResource() {
        //given
        final URI resource = URI.create("");

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> new TokenResponse(resource));

        //then + expected
    }

    @ParameterizedTest
    @MethodSource("nullValuesProvider")
    void testConstructorShouldThrowExceptionWhenCalledWithNulls(
            final URI resource, final String accessToken, final String refreshToken,
            final long expiresIn, final long expiresOn, final int tokenType) {
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
        final TokenResponse actual = new TokenResponse(RESOURCE);

        //then
        Assertions.assertEquals(RESOURCE, actual.resource());
        Assertions.assertEquals(DUMMY_TOKEN, actual.accessToken());
        Assertions.assertEquals(DUMMY_TOKEN, actual.refreshToken());
        Assertions.assertEquals(EXPECTED_EXPIRY, actual.expiresIn());
        Assertions.assertTrue(MIN_EXPIRES_ON <= actual.expiresOn());
        Assertions.assertEquals(TOKEN_TYPE, actual.tokenType());
    }
}
