package com.github.nagyesta.lowkeyvault.model.v7_3.certificate;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.github.nagyesta.lowkeyvault.model.json.util.Base64MimeDeserializer;
import com.github.nagyesta.lowkeyvault.model.json.util.Base64MimeSerializer;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@Data
public class CertificateImportRequest {

    @JsonProperty("pwd")
    private String password;

    @NotNull
    @JsonSerialize(using = Base64MimeSerializer.class)
    @JsonDeserialize(using = Base64MimeDeserializer.class)
    @JsonProperty("value")
    private byte[] certificate;

    @Valid
    @JsonProperty("attributes")
    private CertificatePropertiesModel attributes;

    @JsonProperty("policy")
    private CertificatePolicyModel policy;

    @JsonProperty("tags")
    private Map<String, String> tags;

    public String getCertificateAsString() {
        String value = new String(certificate, StandardCharsets.UTF_8);
        if (!value.contains("BEGIN")) {
            value = Base64.getMimeEncoder().encodeToString(certificate);
        }
        return value;
    }
}
