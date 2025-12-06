package com.github.nagyesta.lowkeyvault.model.v7_3.certificate;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.nagyesta.lowkeyvault.model.json.util.Base64Deserializer;
import com.github.nagyesta.lowkeyvault.model.json.util.Base64Serializer;
import lombok.Data;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;

import java.util.Map;

@Data
public class KeyVaultCertificateItemModel {

    @JsonProperty("id")
    private String certificateId;
    @JsonSerialize(using = Base64Serializer.class)
    @JsonDeserialize(using = Base64Deserializer.class)
    @JsonProperty("x5t")
    private byte[] thumbprint;
    @JsonProperty("attributes")
    private CertificatePropertiesModel attributes;
    @JsonProperty("tags")
    private Map<String, String> tags;

}
