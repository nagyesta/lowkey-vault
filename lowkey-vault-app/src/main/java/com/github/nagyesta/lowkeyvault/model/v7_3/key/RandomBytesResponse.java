package com.github.nagyesta.lowkeyvault.model.v7_3.key;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.nagyesta.lowkeyvault.model.json.util.Base64Deserializer;
import com.github.nagyesta.lowkeyvault.model.json.util.Base64Serializer;
import lombok.Data;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RandomBytesResponse {

    @JsonProperty("value")
    @JsonSerialize(using = Base64Serializer.class)
    @JsonDeserialize(using = Base64Deserializer.class)
    private byte[] value;

    public RandomBytesResponse() {
    }

    public RandomBytesResponse(final byte[] value) {
        this();
        this.value = value;
    }
}
