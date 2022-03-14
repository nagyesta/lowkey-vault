package com.github.nagyesta.lowkeyvault.model.management;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.github.nagyesta.lowkeyvault.model.json.util.EpochSecondsDeserializer;
import com.github.nagyesta.lowkeyvault.model.json.util.EpochSecondsSerializer;
import com.github.nagyesta.lowkeyvault.model.v7_2.common.constants.RecoveryLevel;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.net.URI;
import java.time.OffsetDateTime;

@Data
public class VaultModel {
    @NotNull
    @JsonProperty("baseUri")
    private URI baseUri;
    @NotNull
    @JsonProperty("recoveryLevel")
    private RecoveryLevel recoveryLevel;
    @JsonProperty("recoverableDays")
    private Integer recoverableDays;
    @JsonProperty("created")
    @JsonSerialize(using = EpochSecondsSerializer.class)
    @JsonDeserialize(using = EpochSecondsDeserializer.class)
    private OffsetDateTime createdOn;
    @JsonProperty("deleted")
    @JsonSerialize(using = EpochSecondsSerializer.class)
    @JsonDeserialize(using = EpochSecondsDeserializer.class)
    private OffsetDateTime deletedOn;

}
