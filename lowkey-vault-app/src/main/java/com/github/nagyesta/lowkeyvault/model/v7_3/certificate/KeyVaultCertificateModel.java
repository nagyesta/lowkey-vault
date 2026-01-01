package com.github.nagyesta.lowkeyvault.model.v7_3.certificate;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.nagyesta.lowkeyvault.model.json.util.Base64CertDeserializer;
import com.github.nagyesta.lowkeyvault.model.json.util.Base64CertSerializer;
import com.github.nagyesta.lowkeyvault.model.json.util.Base64Deserializer;
import com.github.nagyesta.lowkeyvault.model.json.util.Base64Serializer;
import lombok.Data;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;

import java.util.Map;

@Data
public class KeyVaultCertificateModel {

    @JsonProperty("id")
    private String id;
    @JsonProperty("kid")
    private String kid;
    @JsonProperty("sid")
    private String sid;
    @JsonSerialize(using = Base64Serializer.class)
    @JsonDeserialize(using = Base64Deserializer.class)
    @JsonProperty("x5t")
    private byte[] thumbprint;
    @JsonSerialize(using = Base64CertSerializer.class)
    @JsonDeserialize(using = Base64CertDeserializer.class)
    @JsonProperty("cer")
    private byte[] certificate;
    @Nullable
    @JsonProperty("attributes")
    private CertificatePropertiesModel attributes;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Nullable
    @JsonProperty("policy")
    private CertificatePolicyModel policy;
    @Nullable
    @JsonProperty("tags")
    private Map<String, String> tags;

}
