package com.github.nagyesta.lowkeyvault.model.v7_2.key;

import java.util.List;

public interface BackupListContainer<E> {

    List<E> getVersions();

    void setVersions(List<E> versions);
}
