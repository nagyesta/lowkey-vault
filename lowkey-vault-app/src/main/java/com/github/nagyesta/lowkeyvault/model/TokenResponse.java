package com.github.nagyesta.lowkeyvault.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.NonNull;
import org.springframework.util.Assert;

import java.net.URI;
import java.time.Instant;

public record TokenResponse(
        @NonNull @JsonProperty("resource") URI resource,
        @NonNull @JsonProperty("access_token") String accessToken,
        @NonNull @JsonProperty("refresh_token") String refreshToken,
        @JsonProperty("expires_in") long expiresIn,
        @JsonProperty("expires_on") long expiresOn,
        @JsonProperty("token_type") String tokenType) {

    private static final long EXPIRES_IN = 48 * 3600L;
    private static final String TOKEN = "dummy";
    private static final String BEARER = "Bearer";

    public TokenResponse(@NotNull final URI resource) {
        this(resource, TOKEN, TOKEN, EXPIRES_IN, Instant.now().plusSeconds(EXPIRES_IN).getEpochSecond(), BEARER);
        Assert.hasText(resource.toString(), "Resource must not be empty");
    }
}
