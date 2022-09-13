package com.github.nagyesta.lowkeyvault.model.v7_3.certificate;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.nagyesta.lowkeyvault.service.certificate.impl.CertAuthorityType;
import lombok.Data;

import java.util.Optional;

@Data
public class IssuerParameterModel {

    @JsonProperty("cert_transparency")
    private boolean certTransparency;

    @JsonProperty("cty")
    private String certType;
    @JsonProperty("name")
    private String issuer;

    public IssuerParameterModel() {
    }

    public IssuerParameterModel(final CertAuthorityType issuer) {
        this();
        this.issuer = Optional.ofNullable(issuer).map(CertAuthorityType::getValue).orElse(null);
    }
}
