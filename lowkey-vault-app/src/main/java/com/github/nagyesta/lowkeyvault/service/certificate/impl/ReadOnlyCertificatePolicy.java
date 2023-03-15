package com.github.nagyesta.lowkeyvault.service.certificate.impl;

import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyCurveName;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyType;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.request.CreateKeyRequest;
import com.github.nagyesta.lowkeyvault.service.key.impl.KeyCreationInput;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Set;

public interface ReadOnlyCertificatePolicy {

    default KeyCreationInput<?> toKeyCreationInput() {
        final CreateKeyRequest request = new CreateKeyRequest();
        request.setKeyType(getKeyType());
        request.setKeySize(getKeySize());
        request.setKeyCurveName(getKeyCurveName());
        return request.toKeyCreationInput();
    }

    default Date certNotBefore() {
        return Date.from(getValidityStart().truncatedTo(ChronoUnit.DAYS).toInstant());
    }


    default Date certExpiry() {
        return Date.from(getValidityStart().plusMonths(getValidityMonths()).truncatedTo(ChronoUnit.DAYS).toInstant());
    }

    String getName();

    CertAuthorityType getCertAuthorityType();

    String getSubject();

    Set<String> getDnsNames();

    Set<String> getEmails();

    Set<String> getUpns();

    int getValidityMonths();

    OffsetDateTime getValidityStart();

    CertContentType getContentType();

    boolean isReuseKeyOnRenewal();

    boolean isExportablePrivateKey();

    KeyType getKeyType();

    KeyCurveName getKeyCurveName();

    Integer getKeySize();

    boolean isEnableTransparency();

    String getCertificateType();

    Set<KeyUsageEnum> getKeyUsage();

    Set<String> getExtendedKeyUsage();
}
