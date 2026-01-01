package com.github.nagyesta.lowkeyvault.model.v7_3.certificate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.nagyesta.lowkeyvault.service.certificate.impl.CertAuthorityType;
import lombok.Data;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

@Data
public class IssuerParameterModel {

    @JsonProperty("cert_transparency")
    private boolean certTransparency;
    @Nullable
    @JsonProperty("cty")
    private String certType;
    @Nullable
    @JsonProperty("name")
    private String issuer;

    @JsonCreator
    public IssuerParameterModel() {
    }

    public IssuerParameterModel(final CertAuthorityType issuer) {
        this();
        this.issuer = Optional.ofNullable(issuer).map(CertAuthorityType::getValue).orElse(null);
    }
}
