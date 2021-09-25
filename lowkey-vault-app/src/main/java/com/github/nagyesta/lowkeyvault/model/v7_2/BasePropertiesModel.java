package com.github.nagyesta.lowkeyvault.model.v7_2;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.github.nagyesta.lowkeyvault.model.json.util.EpochSecondsDeserializer;
import com.github.nagyesta.lowkeyvault.model.json.util.EpochSecondsSerializer;
import com.github.nagyesta.lowkeyvault.model.v7_2.common.constants.RecoveryLevel;
import lombok.Data;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BasePropertiesModel {

    @JsonProperty("enabled")
    private boolean enabled;
    @JsonProperty("created")
    @JsonSerialize(using = EpochSecondsSerializer.class)
    @JsonDeserialize(using = EpochSecondsDeserializer.class)
    private OffsetDateTime createdOn;
    @JsonProperty("updated")
    @JsonSerialize(using = EpochSecondsSerializer.class)
    @JsonDeserialize(using = EpochSecondsDeserializer.class)
    private OffsetDateTime updatedOn;
    @JsonProperty("recoveryLevel")
    private RecoveryLevel recoveryLevel;
    @JsonProperty("exp")
    @JsonSerialize(using = EpochSecondsSerializer.class)
    @JsonDeserialize(using = EpochSecondsDeserializer.class)
    private OffsetDateTime expiresOn;
    @JsonProperty("nbf")
    @JsonSerialize(using = EpochSecondsSerializer.class)
    @JsonDeserialize(using = EpochSecondsDeserializer.class)
    private OffsetDateTime notBefore;
    @JsonProperty("recoverableDays")
    private Integer recoverableDays;

    public BasePropertiesModel() {
        enabled = true;
        recoveryLevel = RecoveryLevel.PURGEABLE;
        final OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        createdOn = now;
        updatedOn = now;
    }
}
