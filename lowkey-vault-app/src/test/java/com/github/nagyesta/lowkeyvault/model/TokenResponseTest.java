package com.github.nagyesta.lowkeyvault.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.Instant;

class TokenResponseTest {

    private static final int EXPECTED_EXPIRY = 48 * 3600;
    private static final Instant NOW = Instant.now();
    private static final Instant EXPIRES_ON = NOW.plusSeconds(EXPECTED_EXPIRY);
    private static final long MIN_EXPIRES_ON = EXPIRES_ON.getEpochSecond();
    private static final String TOKEN_TYPE = "Bearer";
    private static final String DUMMY_TOKEN = "dummy";
    private static final URI RESOURCE = URI.create("https://localhost:8443/path");

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
