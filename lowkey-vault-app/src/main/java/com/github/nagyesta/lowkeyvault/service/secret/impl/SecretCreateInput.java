package com.github.nagyesta.lowkeyvault.service.secret.impl;


import lombok.Data;
import lombok.ToString;
import org.jspecify.annotations.Nullable;
import org.springframework.util.Assert;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@SuppressWarnings("checkstyle:MagicNumber")
@Data
public class SecretCreateInput {

    private final String value;
    @Nullable
    private final String contentType;
    @Nullable
    private final OffsetDateTime createdOn;
    @Nullable
    private final OffsetDateTime updatedOn;
    @Nullable
    private final OffsetDateTime expiresOn;
    @Nullable
    private final OffsetDateTime notBefore;
    private final boolean enabled;
    private final boolean managed;
    private final Map<String, String> tags;

    SecretCreateInput(final SecretCreateInputBuilder builder) {
        Assert.notNull(builder.value, "Secret value cannot be null!");
        this.value = builder.value;
        this.contentType = builder.contentType;
        this.createdOn = builder.createdOn;
        this.updatedOn = builder.updatedOn;
        this.expiresOn = builder.expiresOn;
        this.notBefore = builder.notBefore;
        this.enabled = builder.enabled;
        this.managed = builder.managed;
        this.tags = Map.copyOf(builder.tags);
    }

    public static SecretCreateInputBuilder builder() {
        return new SecretCreateInputBuilder();
    }

    @ToString
    public static class SecretCreateInputBuilder {
        @Nullable
        private String value;
        @Nullable
        private String contentType;
        @Nullable
        private OffsetDateTime createdOn;
        @Nullable
        private OffsetDateTime updatedOn;
        @Nullable
        private OffsetDateTime expiresOn;
        @Nullable
        private OffsetDateTime notBefore;
        private boolean enabled;
        private boolean managed;
        private Map<String, String> tags = Map.of();

        SecretCreateInputBuilder() {
        }

        public SecretCreateInputBuilder value(final String value) {
            this.value = value;
            return this;
        }

        public SecretCreateInputBuilder contentType(@Nullable final String contentType) {
            this.contentType = contentType;
            return this;
        }

        public SecretCreateInputBuilder createdOn(@Nullable final OffsetDateTime createdOn) {
            this.createdOn = createdOn;
            return this;
        }

        public SecretCreateInputBuilder updatedOn(@Nullable final OffsetDateTime updatedOn) {
            this.updatedOn = updatedOn;
            return this;
        }

        public SecretCreateInputBuilder expiresOn(@Nullable final OffsetDateTime expiresOn) {
            this.expiresOn = expiresOn;
            return this;
        }

        public SecretCreateInputBuilder notBefore(@Nullable final OffsetDateTime notBefore) {
            this.notBefore = notBefore;
            return this;
        }

        public SecretCreateInputBuilder enabled(final boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public SecretCreateInputBuilder managed(final boolean managed) {
            this.managed = managed;
            return this;
        }

        public SecretCreateInputBuilder tags(@Nullable final Map<String, String> tags) {
            this.tags = Map.copyOf(Optional.ofNullable(tags).orElse(Collections.emptyMap()));
            return this;
        }

        public SecretCreateInput build() {
            return new SecretCreateInput(this);
        }
    }
}
