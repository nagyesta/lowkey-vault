package com.github.nagyesta.lowkeyvault.http.management;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VaultModel {
    @JsonProperty("baseUri")
    private URI baseUri;
    @JsonProperty("aliases")
    private Set<URI> aliases;
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
