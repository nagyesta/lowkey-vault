package com.github.nagyesta.lowkeyvault.service.certificate.util;

import com.github.nagyesta.lowkeyvault.mapper.v7_2.key.EcJsonWebKeyImportRequestConverter;
import com.github.nagyesta.lowkeyvault.mapper.v7_2.key.RsaJsonWebKeyImportRequestConverter;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyCurveName;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.request.JsonWebKeyImportRequest;
import com.github.nagyesta.lowkeyvault.service.certificate.impl.CertAuthorityType;
import com.github.nagyesta.lowkeyvault.service.certificate.impl.CertificateCreationInput;
import com.github.nagyesta.lowkeyvault.service.certificate.impl.KeyUsageEnum;
import org.bouncycastle.asn1.x509.GeneralName;

import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public final class ParserUtil {

    private static final long HOURS_IN_MONTH = 730L;

    private ParserUtil() {
        throw new IllegalCallerException("Utility cannot be instantiated.");
    }

    public static CertificateCreationInput.CertificateCreationInputBuilder parseCertProperties(
            final X509Certificate certificate) {
        return CertificateCreationInput.builder()
                .certAuthorityType(CertAuthorityType.UNKNOWN)
                .subject(certificate.getSubjectX500Principal().getName())
                .dnsNames(getCertificateAlternativeNamesByType(certificate, GeneralName.dNSName))
                .ips(getCertificateAlternativeNamesByType(certificate, GeneralName.iPAddress))
                .emails(getCertificateAlternativeNamesByType(certificate, GeneralName.rfc822Name))
                .validityMonths(validityMonths(certificate))
                .validityStart(certificate.getNotBefore().toInstant().atOffset(ZoneOffset.UTC))
                .keyUsage(KeyUsageEnum.parseBitString(certificate.getKeyUsage()))
                .extendedKeyUsage(Set.copyOf(extendedKeyUsage(certificate)));
    }

    public static KeyCurveName findKeyCurve(final JsonWebKeyImportRequest keyImportRequest) {
        if (!keyImportRequest.getKeyType().isEc()) {
            return null;
        }
        return new EcJsonWebKeyImportRequestConverter().getKeyParameter(keyImportRequest);
    }

    public static Integer findKeySize(final JsonWebKeyImportRequest keyImportRequest) {
        if (!keyImportRequest.getKeyType().isRsa()) {
            return null;
        }
        return new RsaJsonWebKeyImportRequestConverter().getKeyParameter(keyImportRequest);
    }

    private static List<String> extendedKeyUsage(final X509Certificate certificate) {
        try {
            return Optional.ofNullable(certificate.getExtendedKeyUsage()).orElse(List.of());
        } catch (final CertificateParsingException e) {
            throw new IllegalArgumentException("Failed to get extended key usage.", e);
        }
    }

    private static int validityMonths(final X509Certificate certificate) {
        final Instant notAfter = certificate.getNotAfter().toInstant();
        final Instant notBefore = certificate.getNotBefore().toInstant();
        return calculateValidityMonths(notAfter, notBefore);
    }

    private static int calculateValidityMonths(final Instant notAfter, final Instant notBefore) {
        final Instant end = notAfter.minusSeconds(1);
        int count = 1;
        while (end.minus(count * HOURS_IN_MONTH, ChronoUnit.HOURS).isAfter(notBefore)) {
            count++;
        }
        return count;
    }

    private static Set<String> getCertificateAlternativeNamesByType(final X509Certificate certificate, final int type) {
        try {
            return Optional.ofNullable(certificate.getSubjectAlternativeNames()).orElse(Set.of())
                    .stream()
                    .filter(l -> Objects.equals(type, l.get(0)))
                    .map(l -> (String) l.get(1))
                    .collect(Collectors.toSet());
        } catch (final CertificateParsingException e) {
            throw new IllegalArgumentException("Failed to get alternative names by type: " + type, e);
        }
    }
}
