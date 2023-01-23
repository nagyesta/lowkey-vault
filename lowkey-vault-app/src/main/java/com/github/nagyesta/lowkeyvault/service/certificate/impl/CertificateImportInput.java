package com.github.nagyesta.lowkeyvault.service.certificate.impl;

import com.github.nagyesta.lowkeyvault.mapper.v7_2.key.EcJsonWebKeyImportRequestConverter;
import com.github.nagyesta.lowkeyvault.mapper.v7_2.key.RsaJsonWebKeyImportRequestConverter;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyCurveName;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.request.JsonWebKeyImportRequest;
import com.github.nagyesta.lowkeyvault.model.v7_3.certificate.*;
import lombok.Data;
import lombok.NonNull;
import org.bouncycastle.asn1.x509.GeneralName;
import org.springframework.lang.Nullable;

import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Data
public class CertificateImportInput {

    private static final long HOURS_IN_MONTH = 730L;
    private final String name;
    private final CertificateCreationInput certificateData;

    private final JsonWebKeyImportRequest keyData;

    private final X509Certificate certificate;

    private final CertificatePolicyModel policyModel;

    public CertificateImportInput(@NonNull final String name,
                                  @NonNull final String certificateContent,
                                  @Nullable final String password,
                                  @NonNull final CertContentType contentType,
                                  @NonNull final CertificatePolicyModel policyModel) {
        this.name = name;
        this.certificate = (X509Certificate) contentType.getCertificateChain(certificateContent, password).get(0);
        this.keyData = contentType.getKey(certificateContent, password);
        this.policyModel = policyModel;
        final X509CertificateModel attributes = Objects.requireNonNullElse(policyModel.getX509Properties(), new X509CertificateModel());
        final CertificateKeyModel keyProperties = Objects.requireNonNullElse(policyModel.getKeyProperties(), new CertificateKeyModel());
        final IssuerParameterModel issuer = Objects.requireNonNullElse(policyModel.getIssuer(), new IssuerParameterModel());
        this.certificateData = CertificateCreationInput.builder()
                .name(name)
                .certAuthorityType(CertAuthorityType.UNKNOWN)
                .subject(Optional.ofNullable(attributes.getSubject()).orElse(certificate.getSubjectX500Principal().getName()))
                .dnsNames(alternativeName(attributes, certificate, SubjectAlternativeNames::getDnsNames, GeneralName.dNSName))
                .ips(alternativeName(attributes, certificate, SubjectAlternativeNames::getUpns, GeneralName.iPAddress))
                .emails(alternativeName(attributes, certificate, SubjectAlternativeNames::getEmails, GeneralName.rfc822Name))
                .validityMonths(validityMonths(attributes, certificate))
                .validityStart(certificate.getNotBefore().toInstant().atOffset(ZoneOffset.UTC))
                .contentType(contentType)
                .reuseKeyOnRenewal(keyProperties.isReuseKey())
                .exportablePrivateKey(keyProperties.isExportable())
                .keyType(Objects.requireNonNullElse(keyProperties.getKeyType(), keyData.getKeyType()))
                .keyCurveName(findKeyCurve(keyData, keyProperties))
                .keySize(findKeySize(keyData, keyProperties))
                .enableTransparency(issuer.isCertTransparency())
                .certificateType(issuer.getCertType())
                .keyUsage(Optional.ofNullable(attributes.getKeyUsage())
                        .map(Set::copyOf)
                        .orElse(KeyUsageEnum.parseBitString(certificate.getKeyUsage())))
                .extendedKeyUsage(Optional.ofNullable(attributes.getExtendedKeyUsage())
                        .map(Set::copyOf)
                        .orElse(Set.copyOf(extendedKeyUsage())))
                .build();
    }

    private List<String> extendedKeyUsage() {
        try {
            return Optional.ofNullable(certificate.getExtendedKeyUsage()).orElse(List.of());
        } catch (final CertificateParsingException e) {
            throw new IllegalArgumentException("Failed to get extended key usage.", e);
        }
    }

    private KeyCurveName findKeyCurve(final JsonWebKeyImportRequest keyImportRequest, final CertificateKeyModel keyProperties) {
        if (!keyImportRequest.getKeyType().isEc()) {
            return null;
        }
        return Optional.ofNullable(keyProperties.getKeyCurveName())
                .orElseGet(() -> new EcJsonWebKeyImportRequestConverter().getKeyParameter(keyImportRequest));
    }

    private Integer findKeySize(final JsonWebKeyImportRequest keyImportRequest, final CertificateKeyModel keyProperties) {
        if (!keyImportRequest.getKeyType().isRsa()) {
            return null;
        }
        return Optional.ofNullable(keyProperties.getKeySize())
                .orElseGet(() -> new RsaJsonWebKeyImportRequestConverter().getKeyParameter(keyImportRequest));
    }

    private int validityMonths(final X509CertificateModel attributes, final X509Certificate certificate) {
        final Instant notAfter = certificate.getNotAfter().toInstant();
        final Instant notBefore = certificate.getNotBefore().toInstant();
        return Optional.ofNullable(attributes.getValidityMonths())
                .orElseGet(() -> calculateValidityMonths(notAfter, notBefore));
    }

    private int calculateValidityMonths(final Instant notAfter, final Instant notBefore) {
        final Instant end = notAfter.minusSeconds(1);
        int count = 1;
        while (end.minus(count * HOURS_IN_MONTH, ChronoUnit.HOURS).isAfter(notBefore)) {
            count++;
        }
        return count;
    }

    private static Set<String> alternativeName(final X509CertificateModel attributes,
                                               final X509Certificate certificate,
                                               final Function<SubjectAlternativeNames, Set<String>> function,
                                               final int type) {
        try {
            final Set<String> names = getCertificateAlternativeNamesByType(certificate, type);
            return findAlternativeName(attributes, function, names);
        } catch (final CertificateParsingException e) {
            throw new IllegalArgumentException("Failed to get alternative names by type: " + type, e);
        }
    }

    private static Set<String> findAlternativeName(final X509CertificateModel attributes,
                                                   final Function<SubjectAlternativeNames, Set<String>> function,
                                                   final Set<String> defaultValue) {
        return Optional.ofNullable(attributes.getSubjectAlternativeNames())
                .map(function)
                .map(Set::copyOf)
                .orElse(defaultValue);
    }

    private static Set<String> getCertificateAlternativeNamesByType(final X509Certificate certificate, final int type)
            throws CertificateParsingException {
        return Optional.ofNullable(certificate.getSubjectAlternativeNames()).orElse(Set.of())
                .stream()
                .filter(l -> Objects.equals(type, l.get(0)))
                .map(l -> (String) l.get(1))
                .collect(Collectors.toSet());
    }

}
