package com.github.nagyesta.lowkeyvault.model.common.backup;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.BackupListContainer;
import lombok.EqualsAndHashCode;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@EqualsAndHashCode
public class CertificateBackupList implements BackupListContainer<CertificateBackupListItem> {

    @Valid
    @NotNull
    @Size(min = 1)
    @JsonProperty("versions")
    private List<CertificateBackupListItem> versions = List.of();

    @Override
    public List<CertificateBackupListItem> getVersions() {
        return versions;
    }

    @Override
    public void setVersions(final List<CertificateBackupListItem> versions) {
        this.versions = List.copyOf(versions);
    }

}
