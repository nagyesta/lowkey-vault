package com.github.nagyesta.lowkeyvault.http.management.impl;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.github.nagyesta.lowkeyvault.http.management.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.jspecify.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static com.azure.core.http.ContentType.APPLICATION_JSON;
import static com.github.nagyesta.lowkeyvault.http.management.impl.ResponseEntity.VAULT_MODEL_LIST_TYPE_REF;

@Slf4j
public final class LowkeyVaultManagementClientImpl
        implements LowkeyVaultManagementClient {

    private static final String PING_PATH = "/ping";
    private static final String MANAGEMENT_VAULT_PATH = "/management/vault";
    private static final String MANAGEMENT_VAULT_DELETED_PATH = MANAGEMENT_VAULT_PATH + "/deleted";
    private static final String MANAGEMENT_VAULT_RECOVERY_PATH = MANAGEMENT_VAULT_PATH + "/recover";
    private static final String MANAGEMENT_VAULT_ALIAS_PATH = MANAGEMENT_VAULT_PATH + "/alias";
    private static final String MANAGEMENT_VAULT_PURGE_PATH = MANAGEMENT_VAULT_PATH + "/purge";
    private static final String MANAGEMENT_VAULT_TIME_PATH = MANAGEMENT_VAULT_PATH + "/time";
    private static final String MANAGEMENT_VAULT_TIME_ALL_PATH = MANAGEMENT_VAULT_TIME_PATH + "/all";
    private static final String MANAGEMENT_VAULT_EXPORT_ACTIVE_PATH = MANAGEMENT_VAULT_PATH + "/export";
    private static final String BASE_URI_QUERY_PARAM = "baseUri";
    private static final String ALIAS_URI_ADD_QUERY_PARAM = "add";
    private static final String ALIAS_URI_REMOVE_QUERY_PARAM = "remove";
    private static final String SECONDS_QUERY_PARAM = "seconds";
    private static final String REGENERATE_CERTS_QUERY_PARAM = "regenerateCertificates";
    private final String vaultUrl;
    private final HttpClient instance;
    private final ObjectReader objectReader;
    private final ObjectWriter objectWriter;

    public LowkeyVaultManagementClientImpl(
            final String vaultUrl,
            final HttpClient instance,
            final ObjectMapper objectMapper) {
        this.vaultUrl = vaultUrl;
        this.instance = instance;
        this.objectReader = objectMapper.reader();
        this.objectWriter = objectMapper.writer();
    }

    @Override
    public <T extends Throwable> void verifyConnectivity(
            final int retries,
            final int waitMillis,
            final Supplier<T> exceptionProvider) throws T, InterruptedException {
        final var request = new HttpRequest(HttpMethod.GET, vaultUrl + PING_PATH);
        for (var i = 0; i < retries; i++) {
            Thread.sleep(waitMillis);
            if (isSuccessful(doSend(request))) {
                return;
            }
        }
        throw exceptionProvider.get();
    }

    @Override
    public VaultModel createVault(
            final URI baseUri,
            final RecoveryLevel recoveryLevel,
            @Nullable final Integer recoverableDays) {
        final var body = vaultModelAsString(baseUri, recoveryLevel, recoverableDays);
        final var uri = UriUtil.uriBuilderForPath(vaultUrl, MANAGEMENT_VAULT_PATH);
        final var request = new HttpRequest(HttpMethod.POST, uri.toString())
                .setBody(body)
                .setHeader(HttpHeaderName.CONTENT_TYPE, APPLICATION_JSON);
        return sendAndProcess(request, r -> r.getResponseObject(VaultModel.class));
    }

    @Override
    public List<VaultModel> listVaults() {
        final var uri = UriUtil.uriBuilderForPath(vaultUrl, MANAGEMENT_VAULT_PATH);
        final var request = new HttpRequest(HttpMethod.GET, uri.toString());
        return sendAndProcess(request, r -> r.getResponseObject(VAULT_MODEL_LIST_TYPE_REF));
    }

    @Override
    public List<VaultModel> listDeletedVaults() {
        final var uri = UriUtil.uriBuilderForPath(vaultUrl, MANAGEMENT_VAULT_DELETED_PATH);
        final var request = new HttpRequest(HttpMethod.GET, uri.toString());
        return sendAndProcess(request, r -> r.getResponseObject(VAULT_MODEL_LIST_TYPE_REF));
    }

    @Override
    public boolean delete(final URI baseUri) {
        final var uri = UriUtil.uriBuilderForPath(vaultUrl, MANAGEMENT_VAULT_PATH, Map.of(BASE_URI_QUERY_PARAM, baseUri.toString()));
        final var request = new HttpRequest(HttpMethod.DELETE, uri.toString());
        return sendAndProcess(request, r -> r.getResponseObject(Boolean.class));
    }

    @Override
    public VaultModel recover(final URI baseUri) {
        final var parameters = Map.of(BASE_URI_QUERY_PARAM, baseUri.toString());
        final var uri = UriUtil.uriBuilderForPath(vaultUrl, MANAGEMENT_VAULT_RECOVERY_PATH, parameters);
        final var request = new HttpRequest(HttpMethod.PUT, uri.toString())
                .setHeader(HttpHeaderName.CONTENT_TYPE, APPLICATION_JSON);
        return sendAndProcess(request, r -> r.getResponseObject(VaultModel.class));
    }

    @Override
    public VaultModel addAlias(
            final URI baseUri,
            final URI alias) {
        return performAliasUpdate(new TreeMap<>(
                Map.of(BASE_URI_QUERY_PARAM, baseUri.toString(), ALIAS_URI_ADD_QUERY_PARAM, alias.toString())
        ));
    }

    @Override
    public VaultModel removeAlias(
            final URI baseUri,
            final URI alias) {
        return performAliasUpdate(new TreeMap<>(
                Map.of(BASE_URI_QUERY_PARAM, baseUri.toString(), ALIAS_URI_REMOVE_QUERY_PARAM, alias.toString())
        ));
    }

    @Override
    public boolean purge(final URI baseUri) {
        final var parameters = Map.of(BASE_URI_QUERY_PARAM, baseUri.toString());
        final var uri = UriUtil.uriBuilderForPath(vaultUrl, MANAGEMENT_VAULT_PURGE_PATH, parameters);
        final var request = new HttpRequest(HttpMethod.DELETE, uri.toString());
        return sendAndProcess(request, r -> r.getResponseObject(Boolean.class));
    }

    @Override
    public void timeShift(final TimeShiftContext context) {
        final Map<String, String> parameters = new TreeMap<>();
        parameters.put(SECONDS_QUERY_PARAM, Integer.toString(context.seconds()));
        if (context.regenerateCertificates()) {
            parameters.put(REGENERATE_CERTS_QUERY_PARAM, Boolean.TRUE.toString());
        }
        final var optionalURI = Optional.ofNullable(context.vaultBaseUri());
        optionalURI.ifPresent(uri -> parameters.put(BASE_URI_QUERY_PARAM, uri.toString()));
        final var path = optionalURI.map(u -> MANAGEMENT_VAULT_TIME_PATH).orElse(MANAGEMENT_VAULT_TIME_ALL_PATH);
        final var uri = UriUtil.uriBuilderForPath(vaultUrl, path, parameters);
        final var request = new HttpRequest(HttpMethod.PUT, uri.toString())
                .setHeader(HttpHeaderName.CONTENT_TYPE, APPLICATION_JSON);
        sendRaw(request);
    }

    @Override
    public @Nullable String exportActive() {
        final var uri = UriUtil.uriBuilderForPath(vaultUrl, MANAGEMENT_VAULT_EXPORT_ACTIVE_PATH);
        final var request = new HttpRequest(HttpMethod.GET, uri.toString());
        return sendRaw(request).getResponseBodyAsString();
    }

    @Override
    public String unpackBackup(final byte[] backup) throws IOException {
        try (var byteArrayInputStream = new ByteArrayInputStream(backup);
             var gzipInputStream = new GZIPInputStream(byteArrayInputStream)) {
            final var json = new String(gzipInputStream.readAllBytes());
            return objectReader.readTree(json).toPrettyString();
        }
    }

    @Override
    public byte[] compressBackup(final String backup) throws IOException {
        try (var byteArrayOutputStream = new ByteArrayOutputStream();
             var gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream)) {
            gzipOutputStream.write(backup.getBytes(StandardCharsets.UTF_8));
            gzipOutputStream.flush();
            gzipOutputStream.finish();
            return byteArrayOutputStream.toByteArray();
        }
    }

    private VaultModel performAliasUpdate(final Map<String, String> parameters) {
        final var uri = UriUtil.uriBuilderForPath(vaultUrl, MANAGEMENT_VAULT_ALIAS_PATH, parameters);
        final var request = new HttpRequest(HttpMethod.PATCH, uri.toString())
                .setHeader(HttpHeaderName.CONTENT_TYPE, APPLICATION_JSON);
        return sendAndProcess(request, r -> r.getResponseObject(VaultModel.class));
    }

    String vaultModelAsString(
            final URI baseUri,
            final RecoveryLevel recoveryLevel,
            @Nullable final Integer recoverableDays) {
        try {
            return objectWriter.writeValueAsString(new VaultModel(baseUri, null, recoveryLevel, recoverableDays, null, null));
        } catch (final JsonProcessingException e) {
            throw new LowkeyVaultException("Cannot serialize model:", e);
        }
    }

    <T> T sendAndProcess(
            final HttpRequest request,
            final Function<ResponseEntity, T> conversionFunction) {
        final var responseEntity = sendRaw(request);
        return conversionFunction.apply(responseEntity);
    }

    private ResponseEntity sendRaw(final HttpRequest request) {
        final var responseEntity = doSendNotNull(request);
        if (!responseEntity.isSuccessful()) {
            throw new LowkeyVaultException("Request was not successful. Status: " + responseEntity.getResponseCode());
        }
        return responseEntity;
    }

    private boolean isSuccessful(@Nullable final ResponseEntity responseEntity) {
        return Optional.ofNullable(responseEntity)
                .map(ResponseEntity::isSuccessful)
                .orElse(false);
    }

    private @Nullable ResponseEntity doSend(final HttpRequest request) {
        try {
            return doSendNotNull(request);
        } catch (final LowkeyVaultException e) {
            return null;
        }
    }

    private ResponseEntity doSendNotNull(final HttpRequest request) {
        try (var response = instance.send(request).block()) {
            if (response != null && response.getStatusCode() == HttpStatus.SC_BAD_REQUEST) {
                throw new LowkeyVaultException("Bad request: " + response.getBodyAsString(StandardCharsets.UTF_8).block());
            }
            return new ResponseEntity(Objects.requireNonNull(response), objectReader);
        } catch (final Exception e) {
            log.info("Call to container failed: {}", e.getMessage());
            throw new LowkeyVaultException("Couldn't get a response due to an exception.", e);
        }
    }

}
