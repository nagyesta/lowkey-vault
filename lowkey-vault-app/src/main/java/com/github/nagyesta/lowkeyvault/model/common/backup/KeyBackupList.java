package com.github.nagyesta.lowkeyvault.model.common.backup;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.BackupListContainer;
import com.github.nagyesta.lowkeyvault.model.v7_3.key.KeyRotationPolicyModel;
import lombok.EqualsAndHashCode;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@EqualsAndHashCode
public class KeyBackupList implements BackupListContainer<KeyBackupListItem> {

    @Valid
    @NotNull
    @Size(min = 1)
    @JsonProperty("versions")
    private List<KeyBackupListItem> versions = List.of();

    public List<KeyBackupListItem> getVersions() {
        return versions;
    }

    public void setVersions(final List<KeyBackupListItem> versions) {
        this.versions = List.copyOf(versions);
    }

    @Valid
    @JsonProperty("rotationPolicy")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private KeyRotationPolicyModel keyRotationPolicy;

    public KeyRotationPolicyModel getKeyRotationPolicy() {
        return keyRotationPolicy;
    }

    public void setKeyRotationPolicy(final KeyRotationPolicyModel keyRotationPolicy) {
        this.keyRotationPolicy = keyRotationPolicy;
    }
}
