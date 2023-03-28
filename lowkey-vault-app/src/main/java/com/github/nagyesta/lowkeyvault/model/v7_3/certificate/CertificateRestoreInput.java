package com.github.nagyesta.lowkeyvault.model.v7_3.certificate;

import com.github.nagyesta.lowkeyvault.service.certificate.impl.CertContentType;
import com.github.nagyesta.lowkeyvault.service.certificate.impl.CertificateImportInput;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.springframework.util.Assert;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Objects;

@SuppressWarnings("checkstyle:FinalClass")
@Getter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class CertificateRestoreInput extends CertificateImportInput {

    private final String keyVersion;
    private final CertificatePolicyModel issuancePolicy;
    private final Map<String, String> tags;
    private final OffsetDateTime created;
    private final OffsetDateTime updated;
    private final OffsetDateTime notBefore;
    private final OffsetDateTime expires;
    private final boolean enabled;

    private CertificateRestoreInput(final CertificateRestoreInputBuilder builder) {
        super(builder.name, builder.certificateContent, builder.password, builder.contentType, builder.policy);
        Assert.notNull(builder.keyVersion, "keyVersion is required");
        Assert.notNull(builder.tags, "tags is required");
        Assert.notNull(builder.created, "created is required");
        Assert.notNull(builder.updated, "updated is required");
        Assert.notNull(builder.notBefore, "notBefore is required");
        Assert.notNull(builder.expires, "expires is required");
        Assert.notNull(builder.issuancePolicy, "issuancePolicy is required");
        this.keyVersion = builder.keyVersion;
        this.tags = Map.copyOf(builder.tags);
        this.created = builder.created;
        this.updated = builder.updated;
        this.notBefore = builder.notBefore;
        this.expires = builder.expires;
        this.enabled = builder.enabled;
        this.issuancePolicy = builder.issuancePolicy;
    }

    public static CertificateRestoreInputBuilder builder() {
        return new CertificateRestoreInputBuilder();
    }

    public static final class CertificateRestoreInputBuilder {
        private String keyVersion;
        private String name;
        private String certificateContent;
        private String password;
        private CertContentType contentType;
        private CertificatePolicyModel policy;
        private CertificatePolicyModel issuancePolicy;
        private Map<String, String> tags = Map.of();
        private OffsetDateTime created;
        private OffsetDateTime updated;
        private OffsetDateTime notBefore;
        private OffsetDateTime expires;
        private boolean enabled;

        public CertificateRestoreInputBuilder keyVersion(final String keyVersion) {
            this.keyVersion = keyVersion;
            return this;
        }

        public CertificateRestoreInputBuilder name(final String name) {
            this.name = name;
            return this;
        }

        public CertificateRestoreInputBuilder certificateContent(final String certificateContent) {
            this.certificateContent = certificateContent;
            return this;
        }

        public CertificateRestoreInputBuilder password(final String password) {
            this.password = password;
            return this;
        }

        public CertificateRestoreInputBuilder contentType(final CertContentType contentType) {
            this.contentType = contentType;
            return this;
        }

        public CertificateRestoreInputBuilder policy(final CertificatePolicyModel policy) {
            this.policy = policy;
            return this;
        }

        public CertificateRestoreInputBuilder issuancePolicy(final CertificatePolicyModel issuancePolicy) {
            this.issuancePolicy = issuancePolicy;
            return this;
        }

        public CertificateRestoreInputBuilder tags(final Map<String, String> tags) {
            this.tags = Objects.requireNonNullElse(tags, Map.of());
            return this;
        }

        public CertificateRestoreInputBuilder created(final OffsetDateTime created) {
            this.created = created;
            return this;
        }

        public CertificateRestoreInputBuilder updated(final OffsetDateTime updated) {
            this.updated = updated;
            return this;
        }

        public CertificateRestoreInputBuilder notBefore(final OffsetDateTime notBefore) {
            this.notBefore = notBefore;
            return this;
        }

        public CertificateRestoreInputBuilder expires(final OffsetDateTime expires) {
            this.expires = expires;
            return this;
        }

        public CertificateRestoreInputBuilder enabled(final boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public CertificateRestoreInput build() {
            return new CertificateRestoreInput(this);
        }
    }
}
