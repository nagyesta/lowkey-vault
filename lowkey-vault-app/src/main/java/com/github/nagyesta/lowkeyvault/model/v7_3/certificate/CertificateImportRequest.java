package com.github.nagyesta.lowkeyvault.model.v7_3.certificate;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.github.nagyesta.lowkeyvault.model.json.util.Base64CertDeserializer;
import com.github.nagyesta.lowkeyvault.model.json.util.Base64CertSerializer;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

@Data
public class CertificateImportRequest {

    @JsonProperty("pwd")
    private String password;
    @NotNull
    @JsonSerialize(using = Base64CertSerializer.class)
    @JsonDeserialize(using = Base64CertDeserializer.class)
    @JsonProperty("value")
    private byte[] certificate;
    @Valid
    @JsonProperty("attributes")
    private CertificatePropertiesModel attributes;
    @JsonProperty("policy")
    private CertificatePolicyModel policy;
    @JsonProperty("tags")
    private Map<String, String> tags;

}
