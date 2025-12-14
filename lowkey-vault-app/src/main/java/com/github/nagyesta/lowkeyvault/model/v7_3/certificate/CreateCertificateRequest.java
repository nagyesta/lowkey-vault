package com.github.nagyesta.lowkeyvault.model.v7_3.certificate;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.jspecify.annotations.Nullable;

import java.util.Map;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateCertificateRequest {

    @Nullable
    @JsonProperty("attributes")
    @Valid
    private CertificatePropertiesModel properties;
    @JsonProperty("policy")
    @NotNull
    @Valid
    private CertificatePolicyModel policy;
    @Nullable
    @JsonProperty("tags")
    private Map<String, String> tags;

}
