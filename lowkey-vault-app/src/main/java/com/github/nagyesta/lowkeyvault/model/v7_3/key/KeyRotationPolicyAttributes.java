package com.github.nagyesta.lowkeyvault.model.v7_3.key;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.github.nagyesta.lowkeyvault.model.json.util.EpochSecondsDeserializer;
import com.github.nagyesta.lowkeyvault.model.json.util.EpochSecondsSerializer;
import com.github.nagyesta.lowkeyvault.model.v7_3.key.validator.ExpiryPeriod;
import com.github.nagyesta.lowkeyvault.model.v7_3.key.validator.Restore;
import com.github.nagyesta.lowkeyvault.model.v7_3.key.validator.Update;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.time.OffsetDateTime;
import java.time.Period;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"expiryTime", "created", "updated"})
public class KeyRotationPolicyAttributes {

    @Null(groups = {Update.class})
    @NotNull(groups = {Restore.class})
    @JsonProperty("created")
    @JsonSerialize(using = EpochSecondsSerializer.class)
    @JsonDeserialize(using = EpochSecondsDeserializer.class)
    private OffsetDateTime created;
    @Null(groups = {Update.class})
    @NotNull(groups = {Restore.class})
    @JsonProperty("updated")
    @JsonSerialize(using = EpochSecondsSerializer.class)
    @JsonDeserialize(using = EpochSecondsDeserializer.class)
    private OffsetDateTime updated;
    @NotNull(groups = {Restore.class, Update.class})
    @ExpiryPeriod
    @JsonProperty("expiryTime")
    private Period expiryTime;
}
