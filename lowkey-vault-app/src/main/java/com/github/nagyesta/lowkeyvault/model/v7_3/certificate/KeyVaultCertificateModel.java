package com.github.nagyesta.lowkeyvault.model.v7_3.certificate;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.github.nagyesta.lowkeyvault.model.json.util.Base64CertDeserializer;
import com.github.nagyesta.lowkeyvault.model.json.util.Base64CertSerializer;
import com.github.nagyesta.lowkeyvault.model.json.util.Base64Deserializer;
import com.github.nagyesta.lowkeyvault.model.json.util.Base64Serializer;
import lombok.Data;

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
    @JsonProperty("attributes")
    private CertificatePropertiesModel attributes;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("policy")
    private CertificatePolicyModel policy;
    @JsonProperty("tags")
    private Map<String, String> tags;

}
