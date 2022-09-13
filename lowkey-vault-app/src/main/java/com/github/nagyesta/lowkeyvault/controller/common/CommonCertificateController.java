package com.github.nagyesta.lowkeyvault.controller.common;

import com.github.nagyesta.lowkeyvault.mapper.v7_3.certificate.CertificateEntityToV73CertificateItemModelConverter;
import com.github.nagyesta.lowkeyvault.mapper.v7_3.certificate.CertificateEntityToV73CertificateVersionItemModelConverter;
import com.github.nagyesta.lowkeyvault.mapper.v7_3.certificate.CertificateEntityToV73ModelConverter;
import com.github.nagyesta.lowkeyvault.mapper.v7_3.certificate.CertificateEntityToV73PendingCertificateOperationModelConverter;
import com.github.nagyesta.lowkeyvault.model.v7_3.certificate.*;
import com.github.nagyesta.lowkeyvault.service.certificate.CertificateVaultFake;
import com.github.nagyesta.lowkeyvault.service.certificate.ReadOnlyKeyVaultCertificateEntity;
import com.github.nagyesta.lowkeyvault.service.certificate.id.CertificateEntityId;
import com.github.nagyesta.lowkeyvault.service.certificate.id.VersionedCertificateEntityId;
import com.github.nagyesta.lowkeyvault.service.certificate.impl.CertAuthorityType;
import com.github.nagyesta.lowkeyvault.service.certificate.impl.CertContentType;
import com.github.nagyesta.lowkeyvault.service.certificate.impl.CertificateCreationInput;
import com.github.nagyesta.lowkeyvault.service.vault.VaultFake;
import com.github.nagyesta.lowkeyvault.service.vault.VaultService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;

import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.Set;

import static com.github.nagyesta.lowkeyvault.service.certificate.impl.CertificateCreationInput.DEFAULT_VALIDITY_MONTHS;

@Slf4j
public abstract class CommonCertificateController extends GenericEntityController<CertificateEntityId, VersionedCertificateEntityId,
        ReadOnlyKeyVaultCertificateEntity, KeyVaultCertificateModel, DeletedKeyVaultCertificateModel, KeyVaultCertificateItemModel,
        DeletedKeyVaultCertificateItemModel, CertificateEntityToV73ModelConverter, CertificateEntityToV73CertificateItemModelConverter,
        CertificateEntityToV73CertificateVersionItemModelConverter, CertificateVaultFake> {

    private final CertificateEntityToV73PendingCertificateOperationModelConverter pendingModelConverter;

    protected CommonCertificateController(
            @NonNull final CertificateEntityToV73ModelConverter modelConverter,
            @NonNull final CertificateEntityToV73CertificateItemModelConverter itemModelConverter,
            @NonNull final CertificateEntityToV73CertificateVersionItemModelConverter versionItemModelConverter,
            @lombok.NonNull final CertificateEntityToV73PendingCertificateOperationModelConverter pendingModelConverter,
            @NonNull final VaultService vaultService) {
        super(modelConverter, itemModelConverter, versionItemModelConverter, vaultService, VaultFake::certificateVaultFake);
        this.pendingModelConverter = pendingModelConverter;
    }

    public ResponseEntity<KeyVaultPendingCertificateModel> create(
            @Valid @Pattern(regexp = NAME_PATTERN) final String certificateName,
            final URI baseUri,
            @Valid final CreateCertificateRequest request) {
        log.info("Received request to {} create certificate: {} using API version: {}",
                baseUri.toString(), certificateName, apiVersion());

        final CertificateVaultFake vaultFake = getVaultByUri(baseUri);
        final VersionedCertificateEntityId entityId = createCertificateWithAttributes(vaultFake, certificateName, request);
        final ReadOnlyKeyVaultCertificateEntity readOnlyEntity = vaultFake.getEntities().getReadOnlyEntity(entityId);
        return ResponseEntity.accepted().body(pendingModelConverter.convert(readOnlyEntity, baseUri));
    }

    public ResponseEntity<KeyVaultPendingCertificateModel> pendingCreate(
            @Valid @Pattern(regexp = NAME_PATTERN) final String certificateName,
            final URI baseUri) {
        log.info("Received request to {} get pending create certificate: {} using API version: {}",
                baseUri.toString(), certificateName, apiVersion());
        final CertificateVaultFake vaultFake = getVaultByUri(baseUri);
        final VersionedCertificateEntityId entityId = vaultFake.getEntities().getLatestVersionOfEntity(entityId(baseUri, certificateName));
        final ReadOnlyKeyVaultCertificateEntity readOnlyEntity = vaultFake.getEntities().getReadOnlyEntity(entityId);
        return ResponseEntity.ok(pendingModelConverter.convert(readOnlyEntity, baseUri));
    }

    public ResponseEntity<KeyVaultCertificateModel> get(
            @Valid @Pattern(regexp = NAME_PATTERN) final String certificateName,
            final URI baseUri) {
        log.info("Received request to {} get certificate: {} with version: -LATEST- using API version: {}",
                baseUri.toString(), certificateName, apiVersion());

        return ResponseEntity.ok(getLatestEntityModel(baseUri, certificateName));
    }

    public ResponseEntity<KeyVaultCertificateModel> getWithVersion(
            @Valid @Pattern(regexp = NAME_PATTERN) final String certificateName,
            @Valid @Pattern(regexp = VERSION_NAME_PATTERN) final String certificateVersion,
            final URI baseUri) {
        log.info("Received request to {} get certificate: {} with version: {} using API version: {}",
                baseUri.toString(), certificateName, certificateVersion, apiVersion());

        final ReadOnlyKeyVaultCertificateEntity keyVaultCertificateEntity =
                getEntityByNameAndVersion(baseUri, certificateName, certificateVersion);
        return ResponseEntity.ok(convertDetails(keyVaultCertificateEntity, baseUri));
    }

    @Override
    protected VersionedCertificateEntityId versionedEntityId(final URI baseUri, final String name, final String version) {
        return new VersionedCertificateEntityId(baseUri, name, version);
    }

    @Override
    protected CertificateEntityId entityId(final URI baseUri, final String name) {
        return new CertificateEntityId(baseUri, name);
    }

    private VersionedCertificateEntityId createCertificateWithAttributes(
            final CertificateVaultFake certificateVaultFake, final String certificateName, final CreateCertificateRequest request) {
        final CertificatePropertiesModel properties = Objects.requireNonNullElse(request.getProperties(), new CertificatePropertiesModel());
        final VersionedCertificateEntityId certificateEntityId = certificateVaultFake
                .createCertificateVersion(certificateName, toCertificateCreationInput(certificateName, request));
        certificateVaultFake.addTags(certificateEntityId, request.getTags());
        //no need to set expiry, the generation should take care of it based on the X509 propoerties
        certificateVaultFake.setEnabled(certificateEntityId, properties.isEnabled());
        //no need to set managed property as this endpoint cannot create managed entities by definition
        return certificateEntityId;
    }

    private CertificateCreationInput toCertificateCreationInput(final String certificateName, final CreateCertificateRequest request) {
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
}
