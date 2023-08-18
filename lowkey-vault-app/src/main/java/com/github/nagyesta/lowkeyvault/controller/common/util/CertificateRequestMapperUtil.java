package com.github.nagyesta.lowkeyvault.controller.common.util;

import com.github.nagyesta.lowkeyvault.model.v7_3.certificate.*;
import com.github.nagyesta.lowkeyvault.service.certificate.CertificateLifetimeActionActivity;
import com.github.nagyesta.lowkeyvault.service.certificate.CertificateLifetimeActionTrigger;
import com.github.nagyesta.lowkeyvault.service.certificate.CertificateVaultFake;
import com.github.nagyesta.lowkeyvault.service.certificate.id.VersionedCertificateEntityId;
import com.github.nagyesta.lowkeyvault.service.certificate.impl.*;
import org.springframework.util.Assert;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.github.nagyesta.lowkeyvault.service.certificate.impl.CertificateCreationInput.*;

public final class CertificateRequestMapperUtil {
    private CertificateRequestMapperUtil() {
        throw new IllegalCallerException("Utility cannot be instantiated.");
    }

    public static VersionedCertificateEntityId createCertificateWithAttributes(
            final CertificateVaultFake vault, final String certificateName, final CreateCertificateRequest request) {
        //validate first to avoid creating a cert if lifetime policies are invalid
        validateLifetimeActions(request.getPolicy());
        final CertificatePropertiesModel properties = defaultIfNull(request.getProperties());
        final VersionedCertificateEntityId certificateEntityId = vault
                .createCertificateVersion(certificateName, toCertificateCreationInput(certificateName, request));
        vault.addTags(certificateEntityId, request.getTags());
        //no need to set expiry, the generation should take care of it based on the X509 properties
        vault.setEnabled(certificateEntityId, properties.isEnabled());
        //no need to set managed property as this endpoint cannot create managed entities by definition
        setLifetimeActions(vault, request.getPolicy(), certificateEntityId, CertAuthorityType.SELF_SIGNED);
        return certificateEntityId;
    }

    public static VersionedCertificateEntityId importCertificateWithAttributes(
            final CertificateVaultFake vault, final String certificateName, final CertificateImportRequest request) {
        final CertificatePropertiesModel properties = defaultIfNull(request.getAttributes());
        //conversion must handle validation of lifetime actions as well
        final VersionedCertificateEntityId certificateEntityId = vault
                .importCertificateVersion(certificateName, toCertificateImportInput(certificateName, request));
        vault.addTags(certificateEntityId, request.getTags());
        //no need to set expiry, the generation should take care of it based on the X509 properties
        vault.setEnabled(certificateEntityId, properties.isEnabled());
        //no need to set managed property as this endpoint cannot create managed entities by definition
        final CertAuthorityType certAuthorityType = vault.getEntities()
                .getReadOnlyEntity(certificateEntityId).getOriginalCertificatePolicy().getCertAuthorityType();
        final CertificatePolicyModel policyModel = Objects.requireNonNullElse(request.getPolicy(), new CertificatePolicyModel());
        setLifetimeActions(vault, policyModel, certificateEntityId, certAuthorityType);
        return certificateEntityId;
    }


    public static void updateIssuancePolicy(
            final CertificateVaultFake vault, final VersionedCertificateEntityId entityId, final CertificatePolicyModel request) {
        //validate first to avoid updating a cert if lifetime policies are invalid
        validateLifetimeActions(request);
        final KeyVaultCertificateEntity entity = vault.getEntities().getEntity(entityId, KeyVaultCertificateEntity.class);
        entity.updateIssuancePolicy(convertPolicyToCertificateCreationInput(entityId.id(), request));
        //update lifetime actions
        setLifetimeActions(vault, request, entityId, entity.getOriginalCertificatePolicy().getCertAuthorityType());
    }

    public static String getCertificateAsString(final byte[] certificate) {
        String value = new String(certificate, StandardCharsets.UTF_8);
        if (!value.contains("BEGIN")) {
            value = Base64.getMimeEncoder().encodeToString(certificate);
        }
        return value;
    }

    public static CertificateCreationInput convertPolicyToCertificateCreationInput(
            final String certificateName, final CertificatePolicyModel policy) {
        final X509CertificateModel x509Properties = policy.getX509Properties();
        final IssuerParameterModel issuer = policy.getIssuer();
        final CertificateKeyModel keyProperties = policy.getKeyProperties();
        final Optional<SubjectAlternativeNames> sans = Optional.ofNullable(x509Properties.getSubjectAlternativeNames());
        return CertificateCreationInput.builder()
                .name(certificateName)
                .contentType(CertContentType.byMimeType(policy.getSecretProperties().getContentType()))
                //x509
                .subject(x509Properties.getSubject())
                .dnsNames(sans.map(SubjectAlternativeNames::dnsNames).orElse(Set.of()))
                .emails(sans.map(SubjectAlternativeNames::emails).orElse(Set.of()))
                .upns(sans.map(SubjectAlternativeNames::upns).orElse(Set.of()))
                .keyUsage(Objects.requireNonNullElse(x509Properties.getKeyUsage(), DEFAULT_KEY_USAGES))
                .extendedKeyUsage(Objects.requireNonNullElse(x509Properties.getExtendedKeyUsage(), DEFAULT_EXT_KEY_USAGES))
                .validityMonths(Objects.requireNonNullElse(x509Properties.getValidityMonths(), DEFAULT_VALIDITY_MONTHS))
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

    public static Map<CertificateLifetimeActionActivity, CertificateLifetimeActionTrigger> convertActivityMap(
            final List<CertificateLifetimeActionModel> actions) {
        return actions.stream().collect(Collectors
                .toMap(CertificateLifetimeActionModel::getAction, c -> c.getTrigger().asTriggerEntity()));
    }

    private static CertificatePropertiesModel defaultIfNull(final CertificatePropertiesModel model) {
        return Objects.requireNonNullElse(model, new CertificatePropertiesModel());
    }

    private static void validateLifetimeActions(final CertificatePolicyModel policy) {
        final Integer validityMonths = Objects.requireNonNullElse(policy.getX509Properties().getValidityMonths(),
                DEFAULT_VALIDITY_MONTHS);
        Optional.ofNullable(policy.getLifetimeActions())
                .ifPresent(actions -> actions.forEach(a -> a.getTrigger().validate(validityMonths)));
    }

    private static void setLifetimeActions(
            final CertificateVaultFake certificateVaultFake,
            final CertificatePolicyModel policy,
            final VersionedCertificateEntityId certificateEntityId,
            final CertAuthorityType certAuthorityType) {
        final CertificateLifetimeActionPolicy lifetimeActionPolicy = Optional.ofNullable(policy.getLifetimeActions())
                .map(actions -> new CertificateLifetimeActionPolicy(certificateEntityId, convertActivityMap(actions)))
                .orElse(new DefaultCertificateLifetimeActionPolicy(certificateEntityId, certAuthorityType));
        certificateVaultFake.setLifetimeActionPolicy(lifetimeActionPolicy);
    }

    private static CertificateCreationInput toCertificateCreationInput(
            final String certificateName, final CreateCertificateRequest request) {
        final CertificatePolicyModel policy = request.getPolicy();
        return convertPolicyToCertificateCreationInput(certificateName, policy);
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
