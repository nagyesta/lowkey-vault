package com.github.nagyesta.lowkeyvault.model.common.backup;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.BackupListContainer;
import com.github.nagyesta.lowkeyvault.model.v7_3.key.KeyRotationPolicyModel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.jspecify.annotations.Nullable;

import java.util.List;

@Getter
@EqualsAndHashCode
@ToString
public class KeyBackupList
        implements BackupListContainer<KeyBackupListItem> {

    @Valid
    @NotNull
    @Size(min = 1)
    @JsonProperty("versions")
    private List<KeyBackupListItem> versions = List.of();
    @Nullable
    @Setter
    @Valid
    @JsonProperty("rotationPolicy")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private KeyRotationPolicyModel keyRotationPolicy;

    public void setVersions(final List<KeyBackupListItem> versions) {
        this.versions = List.copyOf(versions);
    }

}
