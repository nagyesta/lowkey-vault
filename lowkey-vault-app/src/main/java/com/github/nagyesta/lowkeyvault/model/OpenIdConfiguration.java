package com.github.nagyesta.lowkeyvault.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;

public record OpenIdConfiguration(
        @JsonProperty("issuer") String issuer,
        @JsonProperty("jwks_uri") URI jwksUri) {
}
