package com.github.nagyesta.lowkeyvault.service.vault;

import com.github.nagyesta.lowkeyvault.model.v7_2.common.constants.RecoveryLevel;

import java.net.URI;
import java.util.List;

public interface VaultService {

    VaultFake findByUri(URI uri);

    VaultFake findByUriIncludeDeleted(URI uri);

    VaultFake create(URI uri);

    VaultFake create(URI baseUri, RecoveryLevel recoveryLevel, Integer recoverableDays);

    List<VaultFake> list();

    List<VaultFake> listDeleted();

    boolean delete(URI uri);

    void recover(URI uri);

    boolean purge(URI uri);

    void timeShift(int offsetSeconds);
}
