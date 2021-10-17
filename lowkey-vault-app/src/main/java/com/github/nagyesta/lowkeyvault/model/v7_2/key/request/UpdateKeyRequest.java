package com.github.nagyesta.lowkeyvault.model.v7_2.key.request;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.nagyesta.lowkeyvault.model.v7_2.BasePropertiesUpdateModel;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyOperation;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class UpdateKeyRequest {

    @JsonProperty("key_ops")
    private List<KeyOperation> keyOperations;

    @JsonProperty("attributes")
    private BasePropertiesUpdateModel properties;

    @JsonProperty("tags")
    private Map<String, String> tags;

}
