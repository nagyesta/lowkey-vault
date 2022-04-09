package com.github.nagyesta.lowkeyvault.http.management;

import lombok.NonNull;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.function.Supplier;

public interface LowkeyVaultManagementClient {

    <T extends Throwable> void verifyConnectivity(int retries, int waitMillis, Supplier<T> exceptionProvider)
            throws T, InterruptedException;

    VaultModel createVault(@NonNull URI baseUri,
                           @NonNull RecoveryLevel recoveryLevel,
                           @NonNull Integer recoverableDays);

    List<VaultModel> listVaults();

    List<VaultModel> listDeletedVaults();

    boolean delete(@NonNull URI baseUri);

    VaultModel recover(@NonNull URI baseUri);

    boolean purge(@NonNull URI baseUri);

    void timeShift(@NonNull TimeShiftContext context);

    String unpackBackup(byte[] backup) throws IOException;

    byte[] compressBackup(String backup) throws IOException;
}
