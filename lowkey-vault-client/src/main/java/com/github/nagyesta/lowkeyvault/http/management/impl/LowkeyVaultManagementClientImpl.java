package com.github.nagyesta.lowkeyvault.http.management.impl;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.github.nagyesta.lowkeyvault.http.management.*;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;
import reactor.util.annotation.Nullable;

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
public final class LowkeyVaultManagementClientImpl implements LowkeyVaultManagementClient {

    private static final String PING_PATH = "/ping";
    private static final String MANAGEMENT_VAULT_PATH = "/management/vault";
    private static final String MANAGEMENT_VAULT_DELETED_PATH = MANAGEMENT_VAULT_PATH + "/deleted";
    private static final String MANAGEMENT_VAULT_RECOVERY_PATH = MANAGEMENT_VAULT_PATH + "/recover";
    private static final String MANAGEMENT_VAULT_PURGE_PATH = MANAGEMENT_VAULT_PATH + "/purge";
    private static final String MANAGEMENT_VAULT_TIME_PATH = MANAGEMENT_VAULT_PATH + "/time";
    private static final String MANAGEMENT_VAULT_TIME_ALL_PATH = MANAGEMENT_VAULT_TIME_PATH + "/all";
    private static final String BASE_URI_QUERY_PARAM = "baseUri";
    private static final String SECONDS_QUERY_PARAM = "seconds";
    private final String vaultUrl;
    private final HttpClient instance;
    private final ObjectReader objectReader;
    private final ObjectWriter objectWriter;

    public LowkeyVaultManagementClientImpl(@NonNull final String vaultUrl,
                                           @NonNull final HttpClient instance,
                                           @NonNull final ObjectMapper objectMapper) {
        this.vaultUrl = vaultUrl;
        this.instance = instance;
        this.objectReader = objectMapper.reader();
        this.objectWriter = objectMapper.writer();
    }

    @Override
    public <T extends Throwable> void verifyConnectivity(
            final int retries, final int waitMillis, @NonNull final Supplier<T> exceptionProvider)
            throws T, InterruptedException {
        final HttpRequest request = new HttpRequest(HttpMethod.GET, vaultUrl + PING_PATH);
        for (int i = 0; i < retries; i++) {
            Thread.sleep(waitMillis);
            if (isSuccessful(doSend(request))) {
                return;
            }
        }
        throw exceptionProvider.get();
    }

    @Override
    public VaultModel createVault(@NonNull final URI baseUri,
                                  @NonNull final RecoveryLevel recoveryLevel,
                                  @Nullable final Integer recoverableDays) {
        final String body = vaultModelAsString(baseUri, recoveryLevel, recoverableDays);
        final URI uri = UriUtil.uriBuilderForPath(vaultUrl, MANAGEMENT_VAULT_PATH);
        final HttpRequest request = new HttpRequest(HttpMethod.POST, uri.toString())
                .setBody(body)
                .setHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON);
        return sendAndProcess(request, r -> r.getResponseObject(VaultModel.class));
    }

    @Override
    public List<VaultModel> listVaults() {
        final URI uri = UriUtil.uriBuilderForPath(vaultUrl, MANAGEMENT_VAULT_PATH);
        final HttpRequest request = new HttpRequest(HttpMethod.GET, uri.toString())
                .setHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON);
        return sendAndProcess(request, r -> r.getResponseObject(VAULT_MODEL_LIST_TYPE_REF));
    }

    @Override
    public List<VaultModel> listDeletedVaults() {
        final URI uri = UriUtil.uriBuilderForPath(vaultUrl, MANAGEMENT_VAULT_DELETED_PATH);
        final HttpRequest request = new HttpRequest(HttpMethod.GET, uri.toString())
                .setHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON);
        return sendAndProcess(request, r -> r.getResponseObject(VAULT_MODEL_LIST_TYPE_REF));
    }

    @Override
    public boolean delete(@NonNull final URI baseUri) {
        final URI uri = UriUtil.uriBuilderForPath(vaultUrl, MANAGEMENT_VAULT_PATH, Map.of(BASE_URI_QUERY_PARAM, baseUri.toString()));
        final HttpRequest request = new HttpRequest(HttpMethod.DELETE, uri.toString())
                .setHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON);
        return sendAndProcess(request, r -> r.getResponseObject(Boolean.class));
    }

    @Override
    public VaultModel recover(@NonNull final URI baseUri) {
        final Map<String, String> parameters = Map.of(BASE_URI_QUERY_PARAM, baseUri.toString());
        final URI uri = UriUtil.uriBuilderForPath(vaultUrl, MANAGEMENT_VAULT_RECOVERY_PATH, parameters);
        final HttpRequest request = new HttpRequest(HttpMethod.PUT, uri.toString())
                .setHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON);
        return sendAndProcess(request, r -> r.getResponseObject(VaultModel.class));
    }

    @Override
    public boolean purge(@NonNull final URI baseUri) {
        final Map<String, String> parameters = Map.of(BASE_URI_QUERY_PARAM, baseUri.toString());
        final URI uri = UriUtil.uriBuilderForPath(vaultUrl, MANAGEMENT_VAULT_PURGE_PATH, parameters);
        final HttpRequest request = new HttpRequest(HttpMethod.DELETE, uri.toString())
                .setHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON);
        return sendAndProcess(request, r -> r.getResponseObject(Boolean.class));
    }

    @Override
    public void timeShift(@NonNull final TimeShiftContext context) {
        final Map<String, String> parameters = new TreeMap<>();
        parameters.put(SECONDS_QUERY_PARAM, Integer.toString(context.getSeconds()));
        final Optional<URI> optionalURI = Optional.ofNullable(context.getVaultBaseUri());
        optionalURI.ifPresent(uri -> parameters.put(BASE_URI_QUERY_PARAM, uri.toString()));
        final String path = optionalURI.map(u -> MANAGEMENT_VAULT_TIME_PATH).orElse(MANAGEMENT_VAULT_TIME_ALL_PATH);
        final URI uri = UriUtil.uriBuilderForPath(vaultUrl, path, parameters);
        final HttpRequest request = new HttpRequest(HttpMethod.PUT, uri.toString())
                .setHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON);
        sendRaw(request);
    }

    @Override
    public String unpackBackup(final byte[] backup) throws IOException {
        final byte[] nonNullBackup = Optional.ofNullable(backup)
                .orElseThrow(() -> new IllegalArgumentException("Backup cannot be null"));
        //noinspection LocalCanBeFinal
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(nonNullBackup);
             GZIPInputStream gzipInputStream = new GZIPInputStream(byteArrayInputStream)) {
            final String json = new String(gzipInputStream.readAllBytes());
            return objectReader.readTree(json).toPrettyString();
        }
    }

    @Override
    public byte[] compressBackup(@NonNull final String backup) throws IOException {
        //noinspection LocalCanBeFinal
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream)) {
            gzipOutputStream.write(backup.getBytes(StandardCharsets.UTF_8));
            gzipOutputStream.flush();
            gzipOutputStream.finish();
            return byteArrayOutputStream.toByteArray();
        }
    }

    String vaultModelAsString(final URI baseUri, final RecoveryLevel recoveryLevel, final Integer recoverableDays) {
        try {
            return objectWriter.writeValueAsString(new VaultModel(baseUri, recoveryLevel, recoverableDays, null, null));
        } catch (final JsonProcessingException e) {
            throw new LowkeyVaultException("Cannot serialize model:", e);
        }
    }

    <T> T sendAndProcess(final HttpRequest request, final Function<ResponseEntity, T> conversionFunction) {
        final ResponseEntity responseEntity = sendRaw(request);
        return conversionFunction.apply(responseEntity);
    }

    private ResponseEntity sendRaw(final HttpRequest request) {
        final ResponseEntity responseEntity = doSendNotNull(request);
        if (!responseEntity.isSuccessful()) {
            throw new LowkeyVaultException("Request was not successful. Status: " + responseEntity.getResponseCode());
        }
        return responseEntity;
    }

    private boolean isSuccessful(final ResponseEntity responseEntity) {
        return Optional.ofNullable(responseEntity)
                .map(ResponseEntity::isSuccessful)
                .orElse(false);
    }

    private ResponseEntity doSend(final HttpRequest request) {
        try {
            return doSendNotNull(request);
        } catch (final LowkeyVaultException e) {
            return null;
        }
    }

    private ResponseEntity doSendNotNull(final HttpRequest request) {
        //noinspection LocalCanBeFinal
        try (HttpResponse response = instance.send(request).block()) {
            return new ResponseEntity(Objects.requireNonNull(response), objectReader);
        } catch (final Exception e) {
            log.info("Call to container failed: {}", e.getMessage());
            throw new LowkeyVaultException("Couldn't get a response due to an exception.", e);
        }
    }

}
