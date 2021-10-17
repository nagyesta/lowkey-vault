package com.github.nagyesta.lowkeyvault.model.v7_2;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.github.nagyesta.lowkeyvault.model.json.util.EpochSecondsDeserializer;
import com.github.nagyesta.lowkeyvault.model.json.util.EpochSecondsSerializer;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BasePropertiesUpdateModel {

    @JsonProperty("enabled")
    private Boolean enabled;
    @JsonProperty("exp")
    @JsonSerialize(using = EpochSecondsSerializer.class)
    @JsonDeserialize(using = EpochSecondsDeserializer.class)
    private OffsetDateTime expiresOn;
    @JsonProperty("nbf")
    @JsonSerialize(using = EpochSecondsSerializer.class)
    @JsonDeserialize(using = EpochSecondsDeserializer.class)
    private OffsetDateTime notBefore;

}
