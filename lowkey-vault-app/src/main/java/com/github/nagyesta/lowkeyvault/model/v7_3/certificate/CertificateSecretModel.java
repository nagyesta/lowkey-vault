package com.github.nagyesta.lowkeyvault.model.v7_3.certificate;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CertificateSecretModel {

    @NotNull
    @JsonProperty("contentType")
    private String contentType;

}
