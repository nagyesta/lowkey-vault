package com.github.nagyesta.lowkeyvault.model.v7_3.certificate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
@JsonIgnoreProperties(value = "lifetime_actions")
public class CertificatePolicyModel {

    @JsonProperty("id")
    private String id;

    @Valid
    @NotNull
    @JsonProperty("key_props")
    private CertificateKeyModel keyProperties;

    @Valid
    @NotNull
    @JsonProperty("secret_props")
    private CertificateSecretModel secretProperties;

    @Valid
    @NotNull
    @JsonProperty("x509_props")
    private X509CertificateModel x509Properties;

    @Valid
    @NotNull
    @JsonProperty("issuer")
    private IssuerParameterModel issuer;

    @Valid
    @JsonProperty("attributes")
    private CertificatePropertiesModel attributes;
}
