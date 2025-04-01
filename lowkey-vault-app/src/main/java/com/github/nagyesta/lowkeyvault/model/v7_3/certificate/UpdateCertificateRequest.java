package com.github.nagyesta.lowkeyvault.model.v7_3.certificate;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@Data
public class UpdateCertificateRequest {

    @JsonProperty("attributes")
    private CertificatePropertiesModel attributes;
    @JsonProperty("tags")
    private Map<String, String> tags;
}
