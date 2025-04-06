package com.github.nagyesta.lowkeyvault.model.v7_2.secret.request;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.nagyesta.lowkeyvault.model.v7_2.BasePropertiesUpdateModel;
import lombok.Data;

import java.util.Map;

@Data
public class UpdateSecretRequest {

    @JsonProperty("contentType")
    private String contentType;
    @JsonProperty("attributes")
    private BasePropertiesUpdateModel properties;
    @JsonProperty("tags")
    private Map<String, String> tags;

}
