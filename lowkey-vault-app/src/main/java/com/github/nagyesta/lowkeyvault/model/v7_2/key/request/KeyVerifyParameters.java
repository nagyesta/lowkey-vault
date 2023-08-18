package com.github.nagyesta.lowkeyvault.model.v7_2.key.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.SignatureAlgorithm;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Base64;
import java.util.Optional;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class KeyVerifyParameters {

    private static final Base64.Decoder DECODER = Base64.getUrlDecoder();

    @NotNull
    @JsonProperty("alg")
    private SignatureAlgorithm algorithm;

    @NotNull
    @NotBlank
    @JsonProperty("value")
    private String value;

    @NotNull
    @NotBlank
    @JsonProperty("digest")
    private String digest;

    @JsonIgnore
    public byte[] getValueAsBase64DecodedBytes() {
        return decodeOptionalStringAsBase64Bytes(value);
    }

    @JsonIgnore
    public byte[] getDigestAsBase64DecodedBytes() {
        return decodeOptionalStringAsBase64Bytes(digest);
    }

    private byte[] decodeOptionalStringAsBase64Bytes(final String digest) {
        return Optional.ofNullable(digest)
                .map(DECODER::decode)
                .orElse(null);
    }
}
