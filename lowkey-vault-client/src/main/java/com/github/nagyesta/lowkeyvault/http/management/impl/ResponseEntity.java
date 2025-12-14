package com.github.nagyesta.lowkeyvault.http.management.impl;

import com.azure.core.http.HttpResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectReader;
import com.github.nagyesta.lowkeyvault.http.management.LowkeyVaultException;
import com.github.nagyesta.lowkeyvault.http.management.VaultModel;
import lombok.Getter;
import org.apache.http.HttpStatus;
import org.jspecify.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.List;

final class ResponseEntity {

    /**
     * The list type needed for JSON serialization.
     */
    public static final ListTypeReference VAULT_MODEL_LIST_TYPE_REF = new ListTypeReference();
    @Getter
    private final int responseCode;
    @Nullable
    private final String responseBody;
    private final ObjectReader reader;

    ResponseEntity(
            final HttpResponse response,
            final ObjectReader reader) {
        this.responseBody = response.getBodyAsString(StandardCharsets.UTF_8).block();
        this.responseCode = response.getStatusCode();
        this.reader = reader;
    }

    public boolean isSuccessful() {
        return responseCode >= HttpStatus.SC_OK && responseCode < HttpStatus.SC_MULTIPLE_CHOICES;
    }

    public <T> T getResponseObject(final Class<T> type) {
        try {
            return reader.forType(type).readValue(responseBody);
        } catch (final JsonProcessingException e) {
            throw new LowkeyVaultException("Unable to map response entity.", e);
        }
    }

    public <T> T getResponseObject(final TypeReference<T> type) {
        try {
            return reader.forType(type).readValue(responseBody);
        } catch (final JsonProcessingException e) {
            throw new LowkeyVaultException("Unable to map response entity.", e);
        }
    }

    public @Nullable String getResponseBodyAsString() {
        return responseBody;
    }

    public static class ListTypeReference extends TypeReference<List<VaultModel>> {

    }
}
