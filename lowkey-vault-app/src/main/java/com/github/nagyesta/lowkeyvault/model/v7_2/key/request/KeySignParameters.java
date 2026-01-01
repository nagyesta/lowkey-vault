package com.github.nagyesta.lowkeyvault.model.v7_2.key.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.SignatureAlgorithm;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.jspecify.annotations.Nullable;

import java.util.Base64;
import java.util.Optional;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class KeySignParameters {

    private static final Base64.Decoder DECODER = Base64.getUrlDecoder();

    @NotNull
    @JsonProperty("alg")
    private SignatureAlgorithm algorithm;
    @NotNull
    @NotBlank
    @JsonProperty("value")
    private String value;

    @JsonIgnore
    public byte @Nullable [] getValueAsBase64DecodedBytes() {
        return Optional.of(value)
                .map(DECODER::decode)
                .orElse(null);
    }
}
