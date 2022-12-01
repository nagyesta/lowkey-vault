package com.github.nagyesta.lowkeyvault.model.v7_3.certificate;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class CertificateSecretModel {

    @NotNull
    @JsonProperty("contentType")
    private String contentType;

}
