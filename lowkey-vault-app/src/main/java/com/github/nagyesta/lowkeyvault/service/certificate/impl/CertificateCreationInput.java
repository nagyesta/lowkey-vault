package com.github.nagyesta.lowkeyvault.service.certificate.impl;

import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyCurveName;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyType;
import lombok.Data;
import lombok.ToString;
import org.jspecify.annotations.Nullable;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.Set;

@Data
public class CertificateCreationInput
        implements ReadOnlyCertificatePolicy {

    /**
     * Default number of months used for certificate validity.
     */
    public static final int DEFAULT_VALIDITY_MONTHS = 12;
    /**
     * Default key usages in case it is not populated during certificate creation in case of EC keys.
     */
    public static final Set<KeyUsageEnum> DEFAULT_EC_KEY_USAGES = Set.of(KeyUsageEnum.DIGITAL_SIGNATURE);
    /**
     * Default key usages in case it is not populated during certificate creation in case of RSA keys.
     */
    public static final Set<KeyUsageEnum> DEFAULT_RSA_KEY_USAGES = Set.of(KeyUsageEnum.DIGITAL_SIGNATURE, KeyUsageEnum.KEY_ENCIPHERMENT);
    /**
     * Default extended key usages in case it is not populated during certificate creation.
     */
    public static final Set<String> DEFAULT_EXT_KEY_USAGES = Set.of("1.3.6.1.5.5.7.3.1", "1.3.6.1.5.5.7.3.2");
    private final String name;
    private final CertAuthorityType certAuthorityType;
    private final String subject;
    private final Set<String> dnsNames;
    private final Set<String> emails;
    private final Set<String> upns;
    private final int validityMonths;
    private final OffsetDateTime validityStart;
    private final CertContentType contentType;
    private final boolean reuseKeyOnRenewal;
    private final boolean exportablePrivateKey;
    private final KeyType keyType;
    @Nullable
    private final KeyCurveName keyCurveName;
    @Nullable
    private final Integer keySize;
    private final boolean enableTransparency;
    @Nullable
    private final String certificateType;
    private final Set<KeyUsageEnum> keyUsage;
    private final Set<String> extendedKeyUsage;

    CertificateCreationInput(final CertificateCreationInputBuilder builder) {
        this.name = Objects.requireNonNull(builder.name);
        this.certAuthorityType = Objects.requireNonNull(builder.certAuthorityType);
        this.subject = Objects.requireNonNull(builder.subject);
        this.dnsNames = Set.copyOf(builder.dnsNames);
        this.emails = Set.copyOf(builder.emails);
        this.upns = Set.copyOf(builder.upns);
        this.validityMonths = builder.validityMonths;
        this.validityStart = Objects.requireNonNull(builder.validityStart);
        this.contentType = Objects.requireNonNull(builder.contentType);
        this.reuseKeyOnRenewal = builder.reuseKeyOnRenewal;
        this.exportablePrivateKey = builder.exportablePrivateKey;
        this.keyType = Objects.requireNonNull(builder.keyType);
        this.keyCurveName = builder.keyCurveName;
        this.keySize = builder.keySize;
        this.enableTransparency = builder.enableTransparency;
        this.certificateType = builder.certificateType;
        this.keyUsage = Set.copyOf(builder.keyUsage);
        this.extendedKeyUsage = Set.copyOf(builder.extendedKeyUsage);
    }

    public static CertificateCreationInputBuilder builder() {
        return new CertificateCreationInputBuilder();
    }

    @ToString
    public static class CertificateCreationInputBuilder {
        @Nullable
        private String name;
        @Nullable
        private CertAuthorityType certAuthorityType;
        @Nullable
        private String subject;
        private Set<String> dnsNames = Set.of();
        private Set<String> emails = Set.of();
        private Set<String> upns = Set.of();
        private int validityMonths;
        @Nullable
        private OffsetDateTime validityStart;
        @Nullable
        private CertContentType contentType;
        private boolean reuseKeyOnRenewal;
        private boolean exportablePrivateKey;
        @Nullable
        private KeyType keyType;
        @Nullable
        private KeyCurveName keyCurveName;
        @Nullable
        private Integer keySize;
        private boolean enableTransparency;
        @Nullable
        private String certificateType;
        private Set<KeyUsageEnum> keyUsage = Set.of();
        private Set<String> extendedKeyUsage = Set.of();

        public CertificateCreationInputBuilder name(final String name) {
            this.name = name;
            return this;
        }

        public CertificateCreationInputBuilder certAuthorityType(final CertAuthorityType certAuthorityType) {
            this.certAuthorityType = certAuthorityType;
            return this;
        }

        public CertificateCreationInputBuilder subject(final String subject) {
            this.subject = subject;
            return this;
        }

        public CertificateCreationInputBuilder dnsNames(final Set<String> dnsNames) {
            this.dnsNames = Set.copyOf(dnsNames);
            return this;
        }

        public CertificateCreationInputBuilder emails(final Set<String> emails) {
            this.emails = Set.copyOf(emails);
            return this;
        }

        public CertificateCreationInputBuilder upns(final Set<String> upns) {
            this.upns = Set.copyOf(upns);
            return this;
        }

        public CertificateCreationInputBuilder validityMonths(final int validityMonths) {
            this.validityMonths = validityMonths;
            return this;
        }

        public CertificateCreationInputBuilder validityStart(final OffsetDateTime validityStart) {
            this.validityStart = validityStart;
            return this;
        }

        public CertificateCreationInputBuilder contentType(final CertContentType contentType) {
            this.contentType = contentType;
            return this;
        }

        public CertificateCreationInputBuilder reuseKeyOnRenewal(final boolean reuseKeyOnRenewal) {
            this.reuseKeyOnRenewal = reuseKeyOnRenewal;
            return this;
        }

        public CertificateCreationInputBuilder exportablePrivateKey(final boolean exportablePrivateKey) {
            this.exportablePrivateKey = exportablePrivateKey;
            return this;
        }

        public CertificateCreationInputBuilder keyType(@Nullable final KeyType keyType) {
            this.keyType = keyType;
            return this;
        }

        public CertificateCreationInputBuilder keyCurveName(@Nullable final KeyCurveName keyCurveName) {
            this.keyCurveName = keyCurveName;
            return this;
        }

        public CertificateCreationInputBuilder keySize(@Nullable final Integer keySize) {
            this.keySize = keySize;
            return this;
        }

        public CertificateCreationInputBuilder enableTransparency(final boolean enableTransparency) {
            this.enableTransparency = enableTransparency;
            return this;
        }

        public CertificateCreationInputBuilder certificateType(@Nullable final String certificateType) {
            this.certificateType = certificateType;
            return this;
        }

        public CertificateCreationInputBuilder keyUsage(final Set<KeyUsageEnum> keyUsage) {
            this.keyUsage = Set.copyOf(keyUsage);
            return this;
        }

        public CertificateCreationInputBuilder extendedKeyUsage(final Set<String> extendedKeyUsage) {
            this.extendedKeyUsage = Set.copyOf(extendedKeyUsage);
            return this;
        }

        public CertificateCreationInput build() {
            return new CertificateCreationInput(this);
        }
    }
}
