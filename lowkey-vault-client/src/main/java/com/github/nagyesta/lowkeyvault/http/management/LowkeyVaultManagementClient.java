package com.github.nagyesta.lowkeyvault.http.management;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.NonNull;
import org.apache.http.HttpException;

import java.net.URI;
import java.util.List;
import java.util.function.Supplier;

public interface LowkeyVaultManagementClient {

    <T extends Throwable> void verifyConnectivity(int retries, int waitMillis, Supplier<T> exceptionProvider)
            throws T, InterruptedException;

    VaultModel createVault(@NonNull URI baseUri,
                           @NonNull RecoveryLevel recoveryLevel,
                           @NonNull Integer recoverableDays) throws HttpException, JsonProcessingException;

    List<VaultModel> listVaults() throws HttpException, JsonProcessingException;

    List<VaultModel> listDeletedVaults() throws HttpException, JsonProcessingException;

    boolean delete(@NonNull URI baseUri) throws HttpException, JsonProcessingException;

    VaultModel recover(@NonNull URI baseUri) throws HttpException, JsonProcessingException;

    boolean purge(@NonNull URI baseUri) throws HttpException, JsonProcessingException;
}
