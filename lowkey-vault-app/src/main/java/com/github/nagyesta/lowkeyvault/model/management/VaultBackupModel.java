package com.github.nagyesta.lowkeyvault.model.management;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.nagyesta.lowkeyvault.model.common.backup.CertificateBackupList;
import com.github.nagyesta.lowkeyvault.model.common.backup.KeyBackupList;
import com.github.nagyesta.lowkeyvault.model.common.backup.SecretBackupList;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.jspecify.annotations.Nullable;

import java.util.Map;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VaultBackupModel {

    @Valid
    @NotNull
    @JsonProperty("attributes")
    private VaultModel attributes;
    @Nullable
    @Valid
    @JsonProperty("keys")
    private Map<String, KeyBackupList> keys;
    @Nullable
    @Valid
    @JsonProperty("secrets")
    private Map<String, SecretBackupList> secrets;
    @Nullable
    @Valid
    @JsonProperty("certificates")
    private Map<String, CertificateBackupList> certificates;
}
