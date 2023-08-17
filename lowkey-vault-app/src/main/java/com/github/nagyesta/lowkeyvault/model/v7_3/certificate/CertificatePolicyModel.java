package com.github.nagyesta.lowkeyvault.model.v7_3.certificate;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
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

    @Valid
    @Size(max = 1) //only one can be set on the UI
    @JsonProperty("lifetime_actions")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<CertificateLifetimeActionModel> lifetimeActions;
}
