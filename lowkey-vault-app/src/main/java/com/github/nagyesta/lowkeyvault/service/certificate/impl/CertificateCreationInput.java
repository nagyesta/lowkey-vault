package com.github.nagyesta.lowkeyvault.service.certificate.impl;

import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyCurveName;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyType;
import lombok.Data;
import lombok.NonNull;
import lombok.ToString;

import java.time.OffsetDateTime;
import java.util.Set;

@Data
public class CertificateCreationInput implements ReadOnlyCertificatePolicy {

    /**
     * Default number of months used for certificate validity.
     */
    public static final int DEFAULT_VALIDITY_MONTHS = 12;
    /**
     * Default key usages in case it is not populated during create certificate in case of EC keys.
     */
    public static final Set<KeyUsageEnum> DEFAULT_EC_KEY_USAGES = Set.of(KeyUsageEnum.DIGITAL_SIGNATURE);
    /**
     * Default key usages in case it is not populated during create certificate in case of RSA keys.
     */
    public static final Set<KeyUsageEnum> DEFAULT_RSA_KEY_USAGES = Set.of(KeyUsageEnum.DIGITAL_SIGNATURE, KeyUsageEnum.KEY_ENCIPHERMENT);
    /**
     * Default extended key usages in case it is not populated during create certificate.
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
    private final KeyCurveName keyCurveName;
    private final Integer keySize;
    private final boolean enableTransparency;
    private final String certificateType;
    private final Set<KeyUsageEnum> keyUsage;
    private final Set<String> extendedKeyUsage;

    CertificateCreationInput(final CertificateCreationInputBuilder builder) {
        this.name = builder.name;
        this.certAuthorityType = builder.certAuthorityType;
        this.subject = builder.subject;
        this.dnsNames = Set.copyOf(builder.dnsNames);
        this.emails = Set.copyOf(builder.emails);
        this.upns = Set.copyOf(builder.upns);
        this.validityMonths = builder.validityMonths;
        this.validityStart = builder.validityStart;
        this.contentType = builder.contentType;
        this.reuseKeyOnRenewal = builder.reuseKeyOnRenewal;
        this.exportablePrivateKey = builder.exportablePrivateKey;
        this.keyType = builder.keyType;
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
        private String name;
        private CertAuthorityType certAuthorityType;
        private String subject;
        private Set<String> dnsNames;
        private Set<String> emails;
        private Set<String> upns;
        private int validityMonths;
        private OffsetDateTime validityStart;
        private CertContentType contentType;
        private boolean reuseKeyOnRenewal;
        private boolean exportablePrivateKey;
        private KeyType keyType;
        private KeyCurveName keyCurveName;
        private Integer keySize;
        private boolean enableTransparency;
        private String certificateType;
        private Set<KeyUsageEnum> keyUsage;
        private Set<String> extendedKeyUsage;

        CertificateCreationInputBuilder() {
            dnsNames = Set.of();
            emails = Set.of();
            upns = Set.of();
            keyUsage = Set.of();
            extendedKeyUsage = Set.of();
        }

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

        public CertificateCreationInputBuilder dnsNames(@NonNull final Set<String> dnsNames) {
            this.dnsNames = Set.copyOf(dnsNames);
            return this;
        }

        public CertificateCreationInputBuilder emails(@NonNull final Set<String> emails) {
            this.emails = Set.copyOf(emails);
            return this;
        }

        public CertificateCreationInputBuilder upns(@NonNull final Set<String> upns) {
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

        public CertificateCreationInputBuilder keyType(final KeyType keyType) {
            this.keyType = keyType;
            return this;
        }

        public CertificateCreationInputBuilder keyCurveName(final KeyCurveName keyCurveName) {
            this.keyCurveName = keyCurveName;
            return this;
        }

        public CertificateCreationInputBuilder keySize(final Integer keySize) {
            this.keySize = keySize;
            return this;
        }

        public CertificateCreationInputBuilder enableTransparency(final boolean enableTransparency) {
            this.enableTransparency = enableTransparency;
            return this;
        }

        public CertificateCreationInputBuilder certificateType(final String certificateType) {
            this.certificateType = certificateType;
            return this;
        }

        public CertificateCreationInputBuilder keyUsage(@NonNull final Set<KeyUsageEnum> keyUsage) {
            this.keyUsage = Set.copyOf(keyUsage);
            return this;
        }

        public CertificateCreationInputBuilder extendedKeyUsage(@NonNull final Set<String> extendedKeyUsage) {
            this.extendedKeyUsage = Set.copyOf(extendedKeyUsage);
            return this;
        }

        public CertificateCreationInput build() {
            return new CertificateCreationInput(this);
        }
    }
}
