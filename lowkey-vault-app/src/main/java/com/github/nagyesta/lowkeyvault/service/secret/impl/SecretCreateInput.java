package com.github.nagyesta.lowkeyvault.service.secret.impl;


import lombok.Data;
import lombok.NonNull;
import lombok.ToString;
import org.springframework.util.Assert;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

@SuppressWarnings("checkstyle:MagicNumber")
@Data
public class SecretCreateInput {

    private final String value;
    private final String contentType;
    private final OffsetDateTime createdOn;
    private final OffsetDateTime updatedOn;
    private final OffsetDateTime expiresOn;
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
        this.tags = Map.copyOf(Objects.requireNonNullElse(builder.tags, Collections.emptyMap()));
    }

    public static SecretCreateInputBuilder builder() {
        return new SecretCreateInputBuilder();
    }

    @ToString
    public static class SecretCreateInputBuilder {
        private String value;
        private String contentType;
        private OffsetDateTime createdOn;
        private OffsetDateTime updatedOn;
        private OffsetDateTime expiresOn;
        private OffsetDateTime notBefore;
        private boolean enabled;
        private boolean managed;
        private Map<String, String> tags;

        SecretCreateInputBuilder() {
        }

        public SecretCreateInputBuilder value(@NonNull final String value) {
            this.value = value;
            return this;
        }

        public SecretCreateInputBuilder contentType(final String contentType) {
            this.contentType = contentType;
            return this;
        }

        public SecretCreateInputBuilder createdOn(final OffsetDateTime createdOn) {
            this.createdOn = createdOn;
            return this;
        }

        public SecretCreateInputBuilder updatedOn(final OffsetDateTime updatedOn) {
            this.updatedOn = updatedOn;
            return this;
        }

        public SecretCreateInputBuilder expiresOn(final OffsetDateTime expiresOn) {
            this.expiresOn = expiresOn;
            return this;
        }

        public SecretCreateInputBuilder notBefore(final OffsetDateTime notBefore) {
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

        public SecretCreateInputBuilder tags(final Map<String, String> tags) {
            this.tags = Map.copyOf(Objects.requireNonNullElse(tags, Collections.emptyMap()));
            return this;
        }

        public SecretCreateInput build() {
            return new SecretCreateInput(this);
        }
    }
}
