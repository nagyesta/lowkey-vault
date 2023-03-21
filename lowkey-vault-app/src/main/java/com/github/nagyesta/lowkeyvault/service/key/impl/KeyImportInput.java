package com.github.nagyesta.lowkeyvault.service.key.impl;


import com.github.nagyesta.lowkeyvault.model.v7_2.key.request.JsonWebKeyImportRequest;
import lombok.Data;
import lombok.NonNull;
import lombok.ToString;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

@SuppressWarnings("checkstyle:MagicNumber")
@Data
public class KeyImportInput {

    private final JsonWebKeyImportRequest key;
    private final OffsetDateTime createdOn;
    private final OffsetDateTime updatedOn;
    private final OffsetDateTime expiresOn;
    private final OffsetDateTime notBefore;
    private final Boolean enabled;
    private final Boolean hsm;
    private final boolean managed;
    private final Map<String, String> tags;

    KeyImportInput(final KeyImportInputBuilder builder) {
        this.key = builder.key;
        this.createdOn = builder.createdOn;
        this.updatedOn = builder.updatedOn;
        this.expiresOn = builder.expiresOn;
        this.notBefore = builder.notBefore;
        this.enabled = builder.enabled;
        this.hsm = builder.hsm;
        this.managed = builder.managed;
        this.tags = Map.copyOf(Objects.requireNonNullElse(builder.tags, Collections.emptyMap()));
    }

    public static KeyImportInputBuilder builder() {
        return new KeyImportInputBuilder();
    }

    @ToString
    public static class KeyImportInputBuilder {
        private JsonWebKeyImportRequest key;
        private OffsetDateTime createdOn;
        private OffsetDateTime updatedOn;
        private OffsetDateTime expiresOn;
        private OffsetDateTime notBefore;
        private Boolean enabled;
        private Boolean hsm;
        private boolean managed;
        private Map<String, String> tags;

        KeyImportInputBuilder() {
        }

        public KeyImportInputBuilder key(@NonNull final JsonWebKeyImportRequest key) {
            this.key = key;
            return this;
        }

        public KeyImportInputBuilder createdOn(final OffsetDateTime createdOn) {
            this.createdOn = createdOn;
            return this;
        }

        public KeyImportInputBuilder updatedOn(final OffsetDateTime updatedOn) {
            this.updatedOn = updatedOn;
            return this;
        }

        public KeyImportInputBuilder expiresOn(final OffsetDateTime expiresOn) {
            this.expiresOn = expiresOn;
            return this;
        }

        public KeyImportInputBuilder notBefore(final OffsetDateTime notBefore) {
            this.notBefore = notBefore;
            return this;
        }

        public KeyImportInputBuilder enabled(final Boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public KeyImportInputBuilder hsm(final Boolean hsm) {
            this.hsm = hsm;
            return this;
        }

        public KeyImportInputBuilder managed(final boolean managed) {
            this.managed = managed;
            return this;
        }

        public KeyImportInputBuilder tags(final Map<String, String> tags) {
            this.tags = Map.copyOf(Objects.requireNonNullElse(tags, Collections.emptyMap()));
            return this;
        }

        public KeyImportInput build() {
            return new KeyImportInput(this);
        }
    }
}
