package com.github.nagyesta.lowkeyvault.service.key.impl;


import com.github.nagyesta.lowkeyvault.model.v7_2.key.request.JsonWebKeyImportRequest;
import lombok.Data;
import lombok.ToString;
import org.jspecify.annotations.Nullable;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

@SuppressWarnings("checkstyle:MagicNumber")
@Data
public class KeyImportInput {

    private final JsonWebKeyImportRequest key;
    @Nullable
    private final OffsetDateTime createdOn;
    @Nullable
    private final OffsetDateTime updatedOn;
    @Nullable
    private final OffsetDateTime expiresOn;
    @Nullable
    private final OffsetDateTime notBefore;
    @Nullable
    private final Boolean enabled;
    @Nullable
    private final Boolean hsm;
    private final boolean managed;
    private final Map<String, String> tags;

    KeyImportInput(final KeyImportInputBuilder builder) {
        this.key = Objects.requireNonNull(builder.key);
        this.createdOn = builder.createdOn;
        this.updatedOn = builder.updatedOn;
        this.expiresOn = builder.expiresOn;
        this.notBefore = builder.notBefore;
        this.enabled = builder.enabled;
        this.hsm = builder.hsm;
        this.managed = builder.managed;
        this.tags = Map.copyOf(builder.tags);
    }

    public static KeyImportInputBuilder builder() {
        return new KeyImportInputBuilder();
    }

    @ToString
    public static class KeyImportInputBuilder {
        @Nullable
        private JsonWebKeyImportRequest key;
        @Nullable
        private OffsetDateTime createdOn;
        @Nullable
        private OffsetDateTime updatedOn;
        @Nullable
        private OffsetDateTime expiresOn;
        @Nullable
        private OffsetDateTime notBefore;
        @Nullable
        private Boolean enabled;
        @Nullable
        private Boolean hsm;
        private boolean managed;
        private Map<String, String> tags = Map.of();

        KeyImportInputBuilder() {
        }

        public KeyImportInputBuilder key(final JsonWebKeyImportRequest key) {
            this.key = key;
            return this;
        }

        public KeyImportInputBuilder createdOn(@Nullable final OffsetDateTime createdOn) {
            this.createdOn = createdOn;
            return this;
        }

        public KeyImportInputBuilder updatedOn(@Nullable final OffsetDateTime updatedOn) {
            this.updatedOn = updatedOn;
            return this;
        }

        public KeyImportInputBuilder expiresOn(@Nullable final OffsetDateTime expiresOn) {
            this.expiresOn = expiresOn;
            return this;
        }

        public KeyImportInputBuilder notBefore(@Nullable final OffsetDateTime notBefore) {
            this.notBefore = notBefore;
            return this;
        }

        public KeyImportInputBuilder enabled(@Nullable final Boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public KeyImportInputBuilder hsm(@Nullable final Boolean hsm) {
            this.hsm = hsm;
            return this;
        }

        public KeyImportInputBuilder managed(final boolean managed) {
            this.managed = managed;
            return this;
        }

        public KeyImportInputBuilder tags(@Nullable final Map<String, String> tags) {
            this.tags = Map.copyOf(Objects.requireNonNullElse(tags, Collections.emptyMap()));
            return this;
        }

        public KeyImportInput build() {
            return new KeyImportInput(this);
        }
    }
}
