package com.github.nagyesta.lowkeyvault.model.v7_3.certificate;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.jspecify.annotations.Nullable;

import java.util.Map;

@Data
public class UpdateCertificateRequest {

    @Nullable
    @JsonProperty("attributes")
    private CertificatePropertiesModel attributes;
    @Nullable
    @JsonProperty("tags")
    private Map<String, String> tags;
}
