package com.github.nagyesta.lowkeyvault.http.management;

import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.function.Supplier;

public interface LowkeyVaultManagementClient {

    <T extends Throwable> void verifyConnectivity(
            int retries,
            int waitMillis, Supplier<T> exceptionProvider) throws T, InterruptedException;

    VaultModel createVault(
            URI baseUri,
            RecoveryLevel recoveryLevel,
            Integer recoverableDays);

    List<VaultModel> listVaults();

    List<VaultModel> listDeletedVaults();

    boolean delete(URI baseUri);

    VaultModel recover(URI baseUri);

    VaultModel addAlias(URI baseUri, URI alias);

    VaultModel removeAlias(URI baseUri, URI alias);

    boolean purge(URI baseUri);

    void timeShift(TimeShiftContext context);

    @Nullable String exportActive();

    String unpackBackup(byte[] backup) throws IOException;

    byte[] compressBackup(String backup) throws IOException;
}
