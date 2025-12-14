package com.github.nagyesta.lowkeyvault.service.certificate.impl;

import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyCurveName;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyType;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.request.CreateKeyRequest;
import com.github.nagyesta.lowkeyvault.service.key.impl.KeyCreationInput;
import org.jspecify.annotations.Nullable;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Set;

@SuppressWarnings("java:S1452") //we don't know it in advance what kind of algorithm we are using
public interface ReadOnlyCertificatePolicy {

    default KeyCreationInput<?> toKeyCreationInput() {
        final var request = new CreateKeyRequest();
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

    @Nullable
    KeyCurveName getKeyCurveName();

    @Nullable
    Integer getKeySize();

    boolean isEnableTransparency();

    @Nullable
    String getCertificateType();

    Set<KeyUsageEnum> getKeyUsage();

    Set<String> getExtendedKeyUsage();
}
