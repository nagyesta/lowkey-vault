package com.github.nagyesta.lowkeyvault.service.certificate.impl;

import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyCurveName;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyType;
import lombok.Data;
import lombok.NonNull;

import java.time.OffsetDateTime;
import java.util.Set;

@Data
public class CertificatePolicy implements ReadOnlyCertificatePolicy {

    private String name;
    private CertAuthorityType certAuthorityType;
    private String subject;
    private Set<String> dnsNames;
    private Set<String> emails;
    private Set<String> ips;
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

    public CertificatePolicy(@NonNull final ReadOnlyCertificatePolicy source) {
        this.name = source.getName();
        this.certAuthorityType = source.getCertAuthorityType();
        this.subject = source.getSubject();
        this.dnsNames = Set.copyOf(source.getDnsNames());
        this.emails = Set.copyOf(source.getEmails());
        this.ips = Set.copyOf(source.getIps());
        this.validityMonths = source.getValidityMonths();
        this.validityStart = source.getValidityStart();
        this.contentType = source.getContentType();
        this.reuseKeyOnRenewal = source.isReuseKeyOnRenewal();
        this.exportablePrivateKey = source.isExportablePrivateKey();
        this.keyType = source.getKeyType();
        this.keyCurveName = source.getKeyCurveName();
        this.keySize = source.getKeySize();
        this.enableTransparency = source.isEnableTransparency();
        this.certificateType = source.getCertificateType();
        this.keyUsage = Set.copyOf(source.getKeyUsage());
        this.extendedKeyUsage = Set.copyOf(source.getExtendedKeyUsage());
    }

    public void setDnsNames(@NonNull final Set<String> dnsNames) {
        this.dnsNames = Set.copyOf(dnsNames);
    }

    public void setEmails(@NonNull final Set<String> emails) {
        this.emails = Set.copyOf(emails);
    }

    public void setIps(@NonNull final Set<String> ips) {
        this.ips = Set.copyOf(ips);
    }

    public void setKeyUsage(@NonNull final Set<KeyUsageEnum> keyUsage) {
        this.keyUsage = Set.copyOf(keyUsage);
    }

    public void setExtendedKeyUsage(@NonNull final Set<String> extendedKeyUsage) {
        this.extendedKeyUsage = Set.copyOf(extendedKeyUsage);
    }
}
