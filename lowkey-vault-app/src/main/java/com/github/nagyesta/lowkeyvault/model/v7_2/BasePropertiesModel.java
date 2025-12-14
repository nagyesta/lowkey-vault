package com.github.nagyesta.lowkeyvault.model.v7_2;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.nagyesta.lowkeyvault.model.json.util.EpochSecondsDeserializer;
import com.github.nagyesta.lowkeyvault.model.json.util.EpochSecondsSerializer;
import com.github.nagyesta.lowkeyvault.model.v7_2.common.constants.RecoveryLevel;
import lombok.Data;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BasePropertiesModel {

    @JsonProperty("enabled")
    private boolean enabled;
    @Nullable
    @JsonProperty("created")
    @JsonSerialize(using = EpochSecondsSerializer.class)
    @JsonDeserialize(using = EpochSecondsDeserializer.class)
    private OffsetDateTime created;
    @Nullable
    @JsonProperty("updated")
    @JsonSerialize(using = EpochSecondsSerializer.class)
    @JsonDeserialize(using = EpochSecondsDeserializer.class)
    private OffsetDateTime updated;
    @Nullable
    @JsonProperty("recoveryLevel")
    private RecoveryLevel recoveryLevel;
    @Nullable
    @JsonProperty("exp")
    @JsonSerialize(using = EpochSecondsSerializer.class)
    @JsonDeserialize(using = EpochSecondsDeserializer.class)
    private OffsetDateTime expiry;
    @Nullable
    @JsonProperty("nbf")
    @JsonSerialize(using = EpochSecondsSerializer.class)
    @JsonDeserialize(using = EpochSecondsDeserializer.class)
    private OffsetDateTime notBefore;
    @Nullable
    @JsonProperty("recoverableDays")
    private Integer recoverableDays;

    public BasePropertiesModel() {
        enabled = true;
        recoveryLevel = RecoveryLevel.PURGEABLE;
        final var now = OffsetDateTime.now(ZoneOffset.UTC);
        created = now;
        updated = now;
    }
}
