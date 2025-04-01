package com.github.nagyesta.lowkeyvault.model.management;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.github.nagyesta.lowkeyvault.model.json.util.EpochSecondsDeserializer;
import com.github.nagyesta.lowkeyvault.model.json.util.EpochSecondsSerializer;
import com.github.nagyesta.lowkeyvault.model.v7_2.common.constants.RecoveryLevel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.Set;

import static com.github.nagyesta.lowkeyvault.openapi.Examples.*;

@Data
public class VaultModel {

    @Schema(example = BASE_URI, description = "The base URI of the vault.")
    @NotNull
    @JsonProperty("baseUri")
    private URI baseUri;
    @Schema(example = ALIASES,
    description = "Optional Vault base URIs that can be used as aliases to access this vault.")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonProperty("aliases")
    private Set<URI> aliases;
    @Schema(example = CUSTOMIZED_RECOVERABLE_PURGEABLE,
            description = "Recovery level of the vault. See: "
                    + "https://docs.microsoft.com/en-us/rest/api/keyvault/secrets/set-secret/set-secret#deletionrecoverylevel")
    @NotNull
    @JsonProperty("recoveryLevel")
    private RecoveryLevel recoveryLevel;
    @Schema(example = FORTY_TWO, minimum = "7", maximum = "90", nullable = true,
            description = "Defines how long the vault will be recoverable after deletion. Acceptable values depend on recovery level.")
    @JsonProperty("recoverableDays")
    private Integer recoverableDays;
    @Schema(example = EPOCH_SECONDS_2022_01_02_AM_03H_04M_05S, nullable = true, implementation = Integer.class, minimum = ONE,
            description = "UTC epoch seconds formatted date time when the vault was created. (Should be null when used for create).")
    @JsonProperty("created")
    @JsonSerialize(using = EpochSecondsSerializer.class)
    @JsonDeserialize(using = EpochSecondsDeserializer.class)
    private OffsetDateTime createdOn;
    @Schema(example = EPOCH_SECONDS_2022_01_02_AM_03H_04M_05S, nullable = true, implementation = Integer.class, minimum = ONE,
            description = "UTC epoch seconds formatted date time when the vault was deleted. (Should be null when used for create).")
    @JsonProperty("deleted")
    @JsonSerialize(using = EpochSecondsSerializer.class)
    @JsonDeserialize(using = EpochSecondsDeserializer.class)
    private OffsetDateTime deletedOn;

}
