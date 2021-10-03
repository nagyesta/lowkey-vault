package com.github.nagyesta.lowkeyvault.model.v7_2.key.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.github.nagyesta.lowkeyvault.model.json.util.Base64Deserializer;
import com.github.nagyesta.lowkeyvault.model.json.util.Base64Serializer;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.EncryptionAlgorithm;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Base64;
import java.util.Optional;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class KeyOperationsParameters {

    private static final Base64.Decoder DECODER = Base64.getUrlDecoder();

    @JsonProperty("aad")
    @JsonSerialize(using = Base64Serializer.class)
    @JsonDeserialize(using = Base64Deserializer.class)
    private byte[] additionalAuthData;

    @NotNull
    @JsonProperty("alg")
    private EncryptionAlgorithm algorithm;

    @JsonProperty("iv")
    @JsonSerialize(using = Base64Serializer.class)
    @JsonDeserialize(using = Base64Deserializer.class)
    private byte[] initializationVector;

    @JsonProperty("tag")
    @JsonSerialize(using = Base64Serializer.class)
    @JsonDeserialize(using = Base64Deserializer.class)
    private byte[] authenticationTag;

    @NotNull
    @NotBlank
    @JsonProperty("value")
    private String value;

    @JsonIgnore
    public byte[] getValueAsBase64DecodedBytes() {
        return Optional.ofNullable(value)
                .map(DECODER::decode)
                .orElse(null);
    }
}
