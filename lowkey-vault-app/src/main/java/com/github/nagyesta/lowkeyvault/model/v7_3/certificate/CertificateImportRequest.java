package com.github.nagyesta.lowkeyvault.model.v7_3.certificate;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.nagyesta.lowkeyvault.model.json.util.Base64CertDeserializer;
import com.github.nagyesta.lowkeyvault.model.json.util.Base64CertSerializer;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;

import java.util.Map;

@Data
public class CertificateImportRequest {

    @Nullable
    @JsonProperty("pwd")
    private String password;
    @NotNull
    @JsonSerialize(using = Base64CertSerializer.class)
    @JsonDeserialize(using = Base64CertDeserializer.class)
    @JsonProperty("value")
    private byte[] certificate;
    @Nullable
    @Valid
    @JsonProperty("attributes")
    private CertificatePropertiesModel attributes;
    @Nullable
    @JsonProperty("policy")
    private CertificatePolicyModel policy;
    @Nullable
    @JsonProperty("tags")
    private Map<String, String> tags;

}
