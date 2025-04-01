package com.github.nagyesta.lowkeyvault.model.v7_2.key.request;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.KeyPropertiesModel;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyCurveName;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyOperation;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyType;
import com.github.nagyesta.lowkeyvault.service.key.impl.EcKeyCreationInput;
import com.github.nagyesta.lowkeyvault.service.key.impl.KeyCreationInput;
import com.github.nagyesta.lowkeyvault.service.key.impl.OctKeyCreationInput;
import com.github.nagyesta.lowkeyvault.service.key.impl.RsaKeyCreationInput;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@SuppressWarnings("checkstyle:MagicNumber")
@Data
public class CreateKeyRequest {

    @NotNull
    @JsonProperty("kty")
    private KeyType keyType;

    @Min(128)
    @Max(4096)
    @JsonProperty("key_size")
    private Integer keySize;

    @JsonProperty("crv")
    private KeyCurveName keyCurveName;

    @JsonProperty("key_ops")
    private List<KeyOperation> keyOperations = List.of();

    @JsonProperty("attributes")
    private KeyPropertiesModel properties;

    @JsonProperty("tags")
    private Map<String, String> tags = Map.of();

    @Min(3)
    @JsonProperty("public_exponent")
    private BigInteger publicExponent;

    public void setPublicExponent(final BigInteger publicExponent) {
        if (BigInteger.ZERO.equals(publicExponent)) {
            this.publicExponent = null;
            return;
        }
        this.publicExponent = publicExponent;
    }

    @JsonIgnore
    @NonNull
    @SuppressWarnings("java:S1452") //at this point we cannot possibly know which algorithm is used
    public KeyCreationInput<?> toKeyCreationInput() {
        if (keyType.isEc()) {
            return new EcKeyCreationInput(keyType, Objects.requireNonNullElse(keyCurveName, KeyCurveName.P_256));
        } else if (keyType.isRsa()) {
            return new RsaKeyCreationInput(keyType, keySize, publicExponent);
        } else {
            Assert.isTrue(keyType.isOct(), "Unknown key type found: " + keyType);
            return new OctKeyCreationInput(keyType, keySize);
        }
    }
}
