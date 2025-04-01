package com.github.nagyesta.lowkeyvault.model.v7_3.certificate;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyCurveName;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CertificateKeyModel {

    @JsonProperty("exportable")
    private boolean exportable;
    @JsonProperty("crv")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private KeyCurveName keyCurveName;
    @NotNull
    @JsonProperty("kty")
    private KeyType keyType;
    @Min(1024)
    @Max(4096)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("key_size")
    private Integer keySize = null;
    @JsonProperty("reuse_key")
    private boolean reuseKey;

}
