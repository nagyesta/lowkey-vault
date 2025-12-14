package com.github.nagyesta.lowkeyvault.service.key.impl;


import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyOperation;
import lombok.Data;
import lombok.ToString;
import org.jspecify.annotations.Nullable;
import org.springframework.util.Assert;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@SuppressWarnings("checkstyle:MagicNumber")
@Data
public class KeyCreateDetailedInput {

    private final KeyCreationInput<?> key;
    private final List<KeyOperation> keyOperations;
    @Nullable
    private final OffsetDateTime expiresOn;
    @Nullable
    private final OffsetDateTime notBefore;
    @Nullable
    private final Boolean enabled;
    private final boolean hsm;
    private final boolean managed;
    private final Map<String, String> tags;

    KeyCreateDetailedInput(final KeyCreateDetailedInputBuilder builder) {
        Assert.notNull(builder.key, "Key parameters cannot be null!");
        this.key = builder.key;
        this.keyOperations = List.copyOf(builder.keyOperations);
        this.expiresOn = builder.expiresOn;
        this.notBefore = builder.notBefore;
        this.enabled = builder.enabled;
        this.hsm = builder.hsm;
        this.managed = builder.managed;
        this.tags = Map.copyOf(builder.tags);
    }

    public static KeyCreateDetailedInputBuilder builder() {
        return new KeyCreateDetailedInputBuilder();
    }

    @ToString
    public static class KeyCreateDetailedInputBuilder {
        @Nullable
        private KeyCreationInput<?> key;
        private List<KeyOperation> keyOperations = List.of();
        @Nullable
        private OffsetDateTime expiresOn;
        @Nullable
        private OffsetDateTime notBefore;
        @Nullable
        private Boolean enabled;
        private boolean hsm;
        private boolean managed;
        private Map<String, String> tags = Map.of();

        KeyCreateDetailedInputBuilder() {
        }

        public KeyCreateDetailedInputBuilder key(final KeyCreationInput<?> key) {
            this.key = key;
            return this;
        }

        public KeyCreateDetailedInputBuilder keyOperations(@Nullable final List<KeyOperation> keyOperations) {
            this.keyOperations = List.copyOf(Objects.requireNonNullElse(keyOperations, Collections.emptyList()));
            return this;
        }

        public KeyCreateDetailedInputBuilder expiresOn(@Nullable final OffsetDateTime expiresOn) {
            this.expiresOn = expiresOn;
            return this;
        }

        public KeyCreateDetailedInputBuilder notBefore(@Nullable final OffsetDateTime notBefore) {
            this.notBefore = notBefore;
            return this;
        }

        public KeyCreateDetailedInputBuilder enabled(@Nullable final Boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public KeyCreateDetailedInputBuilder hsm(final boolean hsm) {
            this.hsm = hsm;
            return this;
        }

        public KeyCreateDetailedInputBuilder managed(final boolean managed) {
            this.managed = managed;
            return this;
        }

        public KeyCreateDetailedInputBuilder tags(@Nullable final Map<String, String> tags) {
            this.tags = Map.copyOf(Objects.requireNonNullElse(tags, Collections.emptyMap()));
            return this;
        }

        public KeyCreateDetailedInput build() {
            return new KeyCreateDetailedInput(this);
        }
    }
}
