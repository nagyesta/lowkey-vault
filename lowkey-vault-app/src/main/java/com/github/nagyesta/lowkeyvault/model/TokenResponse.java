package com.github.nagyesta.lowkeyvault.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import org.springframework.util.Assert;

import java.net.URI;
import java.time.Instant;

public record TokenResponse(
        @JsonProperty("resource") URI resource,
        @JsonProperty("access_token") String accessToken,
        @JsonProperty("refresh_token") String refreshToken,
        @JsonProperty("expires_in") long expiresIn,
        @JsonProperty("expires_on") long expiresOn,
        @JsonProperty("token_type") String tokenType) {

    private static final String BEARER = "Bearer";

    public TokenResponse(
            @NotNull final URI resource,
            @NotNull final String jwt,
            @NotNull final Instant issuedAt,
            @NotNull final Instant expiresOn) {
        this(resource, jwt, jwt, calculateExpiresIn(issuedAt, expiresOn), expiresOn.getEpochSecond(), BEARER);
        Assert.hasText(resource.toString(), "Resource must not be empty");
        Assert.hasText(jwt, "Access token must not be empty");
    }

    private static long calculateExpiresIn(
            final Instant issuedAt,
            final Instant expiresOn) {
        return expiresOn.getEpochSecond() - issuedAt.getEpochSecond();
    }
}
