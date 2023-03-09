package com.github.nagyesta.lowkeyvault.controller.common.util;

import com.github.nagyesta.lowkeyvault.model.v7_3.certificate.*;
import com.github.nagyesta.lowkeyvault.service.certificate.CertificateLifetimeActionActivity;
import com.github.nagyesta.lowkeyvault.service.certificate.CertificateLifetimeActionTrigger;
import com.github.nagyesta.lowkeyvault.service.certificate.CertificateVaultFake;
import com.github.nagyesta.lowkeyvault.service.certificate.id.VersionedCertificateEntityId;
import com.github.nagyesta.lowkeyvault.service.certificate.impl.*;
import org.springframework.util.Assert;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

public final class CertificateRequestMapperUtil {
    private CertificateRequestMapperUtil() {
        throw new IllegalCallerException("Utility cannot be instantiated.");
    }

    public static VersionedCertificateEntityId createCertificateWithAttributes(
            final CertificateVaultFake certificateVaultFake, final String certificateName, final CreateCertificateRequest request) {
        //validate first to avoid creating a cert if lifetime policies are invalid
        validateLifetimeActions(request.getPolicy());
        final CertificatePropertiesModel properties = defaultIfNull(request.getProperties());
        final VersionedCertificateEntityId certificateEntityId = certificateVaultFake
                .createCertificateVersion(certificateName, toCertificateCreationInput(certificateName, request));
        certificateVaultFake.addTags(certificateEntityId, request.getTags());
        //no need to set expiry, the generation should take care of it based on the X509 properties
        certificateVaultFake.setEnabled(certificateEntityId, properties.isEnabled());
        //no need to set managed property as this endpoint cannot create managed entities by definition
        setLifetimeActions(certificateVaultFake, request.getPolicy(), certificateEntityId);
        return certificateEntityId;
    }

    public static VersionedCertificateEntityId importCertificateWithAttributes(
            final CertificateVaultFake certificateVaultFake, final String certificateName, final CertificateImportRequest request) {
        final CertificatePropertiesModel properties = defaultIfNull(request.getAttributes());
        //conversion must handle validation of lifetime actions as well
        final VersionedCertificateEntityId certificateEntityId = certificateVaultFake
                .importCertificateVersion(certificateName, toCertificateImportInput(certificateName, request));
        certificateVaultFake.addTags(certificateEntityId, request.getTags());
        //no need to set expiry, the generation should take care of it based on the X509 properties
        certificateVaultFake.setEnabled(certificateEntityId, properties.isEnabled());
        //no need to set managed property as this endpoint cannot create managed entities by definition
        Optional.ofNullable(request.getPolicy())
                .ifPresent(policyModel -> setLifetimeActions(certificateVaultFake, policyModel, certificateEntityId));
        return certificateEntityId;
    }

    private static CertificatePropertiesModel defaultIfNull(final CertificatePropertiesModel model) {
        return Objects.requireNonNullElse(model, new CertificatePropertiesModel());
    }

    private static void validateLifetimeActions(final CertificatePolicyModel policy) {
        final Integer validityMonths = Objects.requireNonNullElse(policy.getX509Properties().getValidityMonths(),
                CertificateCreationInput.DEFAULT_VALIDITY_MONTHS);
        Optional.ofNullable(policy.getLifetimeActions())
                .ifPresent(actions -> actions.forEach(a -> a.getTrigger().validate(validityMonths)));
    }

    private static void setLifetimeActions(
            final CertificateVaultFake certificateVaultFake,
            final CertificatePolicyModel policy,
            final VersionedCertificateEntityId certificateEntityId) {
        Optional.ofNullable(policy.getLifetimeActions())
                .ifPresent(actions -> certificateVaultFake.setLifetimeActionPolicy(
                        new CertificateLifetimeActionPolicy(certificateEntityId, convertActivityMap(actions))));
    }

    private static Map<CertificateLifetimeActionActivity, CertificateLifetimeActionTrigger> convertActivityMap(
            final List<CertificateLifetimeActionModel> actions) {
        return actions.stream().collect(Collectors
                .toMap(CertificateLifetimeActionModel::getAction, c -> c.getTrigger().asTriggerEntity()));
    }

    private static CertificateCreationInput toCertificateCreationInput(
            final String certificateName, final CreateCertificateRequest request) {
        final X509CertificateModel x509Properties = request.getPolicy().getX509Properties();
        final IssuerParameterModel issuer = request.getPolicy().getIssuer();
        final CertificateKeyModel keyProperties = request.getPolicy().getKeyProperties();
        return CertificateCreationInput.builder()
                .name(certificateName)
                .contentType(CertContentType.byMimeType(request.getPolicy().getSecretProperties().getContentType()))
                //x509
                .subject(x509Properties.getSubject())
                .dnsNames(Objects.requireNonNullElse(x509Properties.getSubjectAlternativeNames().getDnsNames(), Set.of()))
                .emails(Objects.requireNonNullElse(x509Properties.getSubjectAlternativeNames().getEmails(), Set.of()))
                .ips(Objects.requireNonNullElse(x509Properties.getSubjectAlternativeNames().getUpns(), Set.of()))
                .keyUsage(Objects.requireNonNullElse(x509Properties.getKeyUsage(), Set.of()))
                .extendedKeyUsage(Objects.requireNonNullElse(x509Properties.getExtendedKeyUsage(), Set.of()))
                .validityMonths(
                        Objects.requireNonNullElse(x509Properties.getValidityMonths(), CertificateCreationInput.DEFAULT_VALIDITY_MONTHS))
                .validityStart(OffsetDateTime.now())
                //issuer
                .certificateType(issuer.getCertType())
                .enableTransparency(issuer.isCertTransparency())
                //ignore issuer as only self-signed is supported
                .certAuthorityType(CertAuthorityType.SELF_SIGNED)
                //key
                .exportablePrivateKey(keyProperties.isExportable())
                .reuseKeyOnRenewal(keyProperties.isReuseKey())
                .keyType(keyProperties.getKeyType())
                .keyCurveName(keyProperties.getKeyCurveName())
                .keySize(keyProperties.getKeySize())
                //build
                .build();
    }

    private static CertificateImportInput toCertificateImportInput(
            final String certificateName, final CertificateImportRequest request) {
        final CertificatePolicyModel policyModel = Objects.requireNonNullElse(request.getPolicy(), new CertificatePolicyModel());
        return new CertificateImportInput(
                certificateName,
                request.getCertificateAsString(),
                request.getPassword(),
                determineContentType(request),
                policyModel);
    }

    private static CertContentType determineContentType(final CertificateImportRequest request) {
        CertContentType parsed = CertContentType.PKCS12;
        if (request.getCertificateAsString().contains("BEGIN")) {
            parsed = CertContentType.PEM;
        }
        if (request.getPolicy() != null
                && request.getPolicy().getSecretProperties() != null
                && request.getPolicy().getSecretProperties().getContentType() != null) {
            final String contentType = request.getPolicy().getSecretProperties().getContentType();
            Assert.isTrue(parsed.getMimeType().equals(contentType),
                    "Content type must match certificate content when provided.");
        }
        return parsed;
    }
}
