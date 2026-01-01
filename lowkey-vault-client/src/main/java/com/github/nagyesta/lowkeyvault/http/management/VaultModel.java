package com.github.nagyesta.lowkeyvault.http.management;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;

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
    @Nullable
    private Set<URI> aliases;
    @JsonProperty("recoveryLevel")
    private RecoveryLevel recoveryLevel;
    @Nullable
    @JsonProperty("recoverableDays")
    private Integer recoverableDays;
    @Nullable
    @JsonProperty("created")
    @JsonSerialize(using = EpochSecondsSerializer.class)
    @JsonDeserialize(using = EpochSecondsDeserializer.class)
    private OffsetDateTime createdOn;
    @Nullable
    @JsonProperty("deleted")
    @JsonSerialize(using = EpochSecondsSerializer.class)
    @JsonDeserialize(using = EpochSecondsDeserializer.class)
    private OffsetDateTime deletedOn;
}
