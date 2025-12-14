package com.github.nagyesta.lowkeyvault.model.v7_2;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.nagyesta.lowkeyvault.model.json.util.EpochSecondsDeserializer;
import com.github.nagyesta.lowkeyvault.model.json.util.EpochSecondsSerializer;
import lombok.Data;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;

import java.time.OffsetDateTime;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BasePropertiesUpdateModel {

    @Nullable
    @JsonProperty("enabled")
    private Boolean enabled;
    @Nullable
    @JsonProperty("exp")
    @JsonSerialize(using = EpochSecondsSerializer.class)
    @JsonDeserialize(using = EpochSecondsDeserializer.class)
    private OffsetDateTime expiresOn;
    @Nullable
    @JsonProperty("nbf")
    @JsonSerialize(using = EpochSecondsSerializer.class)
    @JsonDeserialize(using = EpochSecondsDeserializer.class)
    private OffsetDateTime notBefore;

}
