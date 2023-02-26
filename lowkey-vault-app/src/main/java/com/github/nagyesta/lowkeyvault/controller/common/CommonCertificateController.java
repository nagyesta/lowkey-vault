package com.github.nagyesta.lowkeyvault.controller.common;

import com.github.nagyesta.lowkeyvault.mapper.v7_3.certificate.CertificateEntityToV73CertificateItemModelConverter;
import com.github.nagyesta.lowkeyvault.mapper.v7_3.certificate.CertificateEntityToV73CertificateVersionItemModelConverter;
import com.github.nagyesta.lowkeyvault.mapper.v7_3.certificate.CertificateEntityToV73ModelConverter;
import com.github.nagyesta.lowkeyvault.mapper.v7_3.certificate.CertificateEntityToV73PendingCertificateOperationModelConverter;
import com.github.nagyesta.lowkeyvault.model.common.KeyVaultItemListModel;
import com.github.nagyesta.lowkeyvault.model.v7_3.certificate.*;
import com.github.nagyesta.lowkeyvault.service.certificate.CertificateVaultFake;
import com.github.nagyesta.lowkeyvault.service.certificate.ReadOnlyKeyVaultCertificateEntity;
import com.github.nagyesta.lowkeyvault.service.certificate.id.CertificateEntityId;
import com.github.nagyesta.lowkeyvault.service.certificate.id.VersionedCertificateEntityId;
import com.github.nagyesta.lowkeyvault.service.certificate.impl.CertAuthorityType;
import com.github.nagyesta.lowkeyvault.service.certificate.impl.CertContentType;
import com.github.nagyesta.lowkeyvault.service.certificate.impl.CertificateCreationInput;
import com.github.nagyesta.lowkeyvault.service.certificate.impl.CertificateImportInput;
import com.github.nagyesta.lowkeyvault.service.vault.VaultFake;
import com.github.nagyesta.lowkeyvault.service.vault.VaultService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static com.github.nagyesta.lowkeyvault.service.certificate.impl.CertificateCreationInput.DEFAULT_VALIDITY_MONTHS;

@Slf4j
public abstract class CommonCertificateController extends GenericEntityController<CertificateEntityId, VersionedCertificateEntityId,
        ReadOnlyKeyVaultCertificateEntity, KeyVaultCertificateModel, DeletedKeyVaultCertificateModel, KeyVaultCertificateItemModel,
        DeletedKeyVaultCertificateItemModel, CertificateEntityToV73ModelConverter, CertificateEntityToV73CertificateItemModelConverter,
        CertificateEntityToV73CertificateVersionItemModelConverter, CertificateVaultFake> {

    /**
     * Default parameter value for including the pending certificates.
     */
    protected static final String TRUE = "true";
    /**
     * Parameter name for including the pending certificates.
     */
    protected static final String INCLUDE_PENDING_PARAM = "includePending";
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
        final VersionedCertificateEntityId entityId = vaultFake
                .getEntities().getLatestVersionOfEntity(entityId(baseUri, certificateName));
        final ReadOnlyKeyVaultCertificateEntity readOnlyEntity = vaultFake
                .getEntities().getReadOnlyEntity(entityId);
        return ResponseEntity.ok(pendingModelConverter.convert(readOnlyEntity, baseUri));
    }

    public ResponseEntity<KeyVaultPendingCertificateModel> pendingDelete(
            @Valid @Pattern(regexp = NAME_PATTERN) final String certificateName,
            final URI baseUri) {
        log.info("Received request to {} get pending delete certificate: {} using API version: {}",
                baseUri.toString(), certificateName, apiVersion());
        final CertificateVaultFake vaultFake = getVaultByUri(baseUri);
        final VersionedCertificateEntityId entityId = vaultFake.getDeletedEntities()
                .getLatestVersionOfEntity(entityId(baseUri, certificateName));
        final ReadOnlyKeyVaultCertificateEntity readOnlyEntity = vaultFake
                .getDeletedEntities().getReadOnlyEntity(entityId);
        return ResponseEntity.ok(pendingModelConverter.convert(readOnlyEntity, baseUri));
    }

    public ResponseEntity<KeyVaultCertificateModel> get(
            @Valid @Pattern(regexp = NAME_PATTERN) final String certificateName,
            final URI baseUri) {
        log.info("Received request to {} get certificate: {} with version: -LATEST- using API version: {}",
                baseUri.toString(), certificateName, apiVersion());

        return ResponseEntity.ok(getLatestEntityModel(baseUri, certificateName));
    }

    public ResponseEntity<CertificatePolicyModel> getPolicy(
            @Valid @Pattern(regexp = NAME_PATTERN) final String certificateName,
            final URI baseUri) {
        log.info("Received request to {} get certificate policy: {} with version: -LATEST- using API version: {}",
                baseUri.toString(), certificateName, apiVersion());

        return ResponseEntity.ok(getLatestEntityModel(baseUri, certificateName).getPolicy());
    }

    public ResponseEntity<KeyVaultCertificateModel> getWithVersion(
            @Valid @Pattern(regexp = NAME_PATTERN) final String certificateName,
            @Valid @Pattern(regexp = VERSION_NAME_PATTERN) final String certificateVersion,
            final URI baseUri) {
        log.info("Received request to {} get certificate: {} with version: {} using API version: {}",
                baseUri.toString(), certificateName, certificateVersion, apiVersion());

        return ResponseEntity.ok(getSpecificEntityModel(baseUri, certificateName, certificateVersion));
    }

    public ResponseEntity<KeyVaultCertificateModel> importCertificate(
            @Valid @Pattern(regexp = NAME_PATTERN) final String certificateName,
            final URI baseUri,
            @Valid @RequestBody final CertificateImportRequest request) {
        log.info("Received request to {} import certificate: {} using API version: {}",
                baseUri.toString(), certificateName, apiVersion());

        final CertificateVaultFake vaultFake = getVaultByUri(baseUri);
        final VersionedCertificateEntityId entityId = importCertificateWithAttributes(vaultFake, certificateName, request);
        final ReadOnlyKeyVaultCertificateEntity readOnlyEntity = vaultFake.getEntities().getReadOnlyEntity(entityId);
        return ResponseEntity.ok().body(convertDetails(readOnlyEntity, baseUri));
    }

    public ResponseEntity<DeletedKeyVaultCertificateModel> delete(
            @Valid @Pattern(regexp = NAME_PATTERN) final String certificateName,
            final URI baseUri) {
        log.info("Received request to {} delete certificate: {} using API version: {}",
                baseUri.toString(), certificateName, apiVersion());

        final CertificateVaultFake vaultFake = getVaultByUri(baseUri);
        final CertificateEntityId entityId = new CertificateEntityId(baseUri, certificateName);
        vaultFake.delete(entityId);
        final VersionedCertificateEntityId latestVersion = vaultFake.getDeletedEntities().getLatestVersionOfEntity(entityId);
        return ResponseEntity.ok(getDeletedModelById(vaultFake, latestVersion, baseUri, true));
    }

    public ResponseEntity<DeletedKeyVaultCertificateModel> getDeletedCertificate(
            @Valid @Pattern(regexp = NAME_PATTERN) final String certificateName,
            final URI baseUri) {
        log.info("Received request to {} get deleted certificate: {} using API version: {}",
                baseUri.toString(), certificateName, apiVersion());

        final CertificateVaultFake vaultFake = getVaultByUri(baseUri);
        final CertificateEntityId entityId = new CertificateEntityId(baseUri, certificateName);
        final VersionedCertificateEntityId latestVersion = vaultFake.getDeletedEntities().getLatestVersionOfEntity(entityId);
        return ResponseEntity.ok(getDeletedModelById(vaultFake, latestVersion, baseUri, false));
    }

    public ResponseEntity<KeyVaultCertificateModel> recoverDeletedCertificate(
            @Valid @Pattern(regexp = NAME_PATTERN) final String certificateName,
            final URI baseUri) {
        log.info("Received request to {} recover deleted certificate: {} using API version: {}",
                baseUri.toString(), certificateName, apiVersion());

        final CertificateVaultFake vaultFake = getVaultByUri(baseUri);
        final CertificateEntityId entityId = new CertificateEntityId(baseUri, certificateName);
        vaultFake.recover(entityId);
        final VersionedCertificateEntityId latestVersion = vaultFake.getEntities().getLatestVersionOfEntity(entityId);
        return ResponseEntity.ok(getModelById(vaultFake, latestVersion, baseUri, true));
    }

    public ResponseEntity<Void> purgeDeleted(
            @Valid @Pattern(regexp = NAME_PATTERN) final String certificateName,
            final URI baseUri) {
        log.info("Received request to {} purge deleted certificate: {} using API version: {}",
                baseUri.toString(), certificateName, apiVersion());

        final CertificateVaultFake vaultFake = getVaultByUri(baseUri);
        final CertificateEntityId entityId = new CertificateEntityId(baseUri, certificateName);
        vaultFake.purge(entityId);
        return ResponseEntity.noContent().build();
    }

    public ResponseEntity<KeyVaultItemListModel<KeyVaultCertificateItemModel>> versions(
            @Valid @Pattern(regexp = NAME_PATTERN) final String certificateName,
            final URI baseUri,
            final int maxResults,
            final int skipToken) {
        log.info("Received request to {} list certificate versions: {} , (max results: {}, skip: {}) using API version: {}",
                baseUri.toString(), certificateName, maxResults, skipToken, apiVersion());

        return ResponseEntity.ok(getPageOfItemVersions(
                baseUri, certificateName, maxResults, skipToken, "/certificates/" + certificateName + "/versions"));
    }

    public ResponseEntity<KeyVaultItemListModel<KeyVaultCertificateItemModel>> listCertificates(
            final URI baseUri,
            final int maxResults,
            final int skipToken,
            final boolean includePending) {
        log.info("Received request to {} list certificates, (max results: {}, skip: {}, includePending: {}) using API version: {}",
                baseUri.toString(), maxResults, skipToken, includePending, apiVersion());

        return ResponseEntity.ok(getPageOfItems(baseUri, maxResults, skipToken, includePending));
    }

    public ResponseEntity<KeyVaultItemListModel<DeletedKeyVaultCertificateItemModel>> listDeletedCertificates(
            final URI baseUri,
            final int maxResults,
            final int skipToken,
            final boolean includePending) {
        log.info("Received request to {} list deleted certificates, (max results: {}, skip: {}, includePending: {}) using API version: {}",
                baseUri.toString(), maxResults, skipToken, includePending, apiVersion());

        return ResponseEntity.ok(getPageOfDeletedItems(baseUri, maxResults, skipToken, includePending));
    }

    private KeyVaultItemListModel<KeyVaultCertificateItemModel> getPageOfItems(
            final URI baseUri, final int limit, final int offset, final boolean includePending) {
        final KeyVaultItemListModel<KeyVaultCertificateItemModel> page =
                super.getPageOfItems(baseUri, limit, offset, "/certificates");
        return fixNextLink(page, includePending);
    }

    private KeyVaultItemListModel<DeletedKeyVaultCertificateItemModel> getPageOfDeletedItems(
            final URI baseUri, final int limit, final int offset, final boolean includePending) {
        final KeyVaultItemListModel<DeletedKeyVaultCertificateItemModel> page =
                super.getPageOfDeletedItems(baseUri, limit, offset, "/deletedcertificates");
        return fixNextLink(page, includePending);
    }

    @Override
    protected VersionedCertificateEntityId versionedEntityId(final URI baseUri, final String name, final String version) {
        return new VersionedCertificateEntityId(baseUri, name, version);
    }

    @Override
    protected CertificateEntityId entityId(final URI baseUri, final String name) {
        return new CertificateEntityId(baseUri, name);
    }

    private <LI> KeyVaultItemListModel<LI> fixNextLink(
            final KeyVaultItemListModel<LI> page,
            final boolean includePending) {
        final String nextLink = Optional.ofNullable(page.getNextLink())
                .map(next -> next + "&" + INCLUDE_PENDING_PARAM + "=" + includePending)
                .orElse(null);
        page.setNextLink(nextLink);
        return page;
    }

    private VersionedCertificateEntityId createCertificateWithAttributes(
            final CertificateVaultFake certificateVaultFake, final String certificateName, final CreateCertificateRequest request) {
        final CertificatePropertiesModel properties = Objects.requireNonNullElse(request.getProperties(), new CertificatePropertiesModel());
        final VersionedCertificateEntityId certificateEntityId = certificateVaultFake
                .createCertificateVersion(certificateName, toCertificateCreationInput(certificateName, request));
        certificateVaultFake.addTags(certificateEntityId, request.getTags());
        //no need to set expiry, the generation should take care of it based on the X509 properties
        certificateVaultFake.setEnabled(certificateEntityId, properties.isEnabled());
        //no need to set managed property as this endpoint cannot create managed entities by definition
        return certificateEntityId;
    }

    private VersionedCertificateEntityId importCertificateWithAttributes(
            final CertificateVaultFake certificateVaultFake, final String certificateName, final CertificateImportRequest request) {
        final CertificatePropertiesModel properties = Objects.requireNonNullElse(request.getAttributes(), new CertificatePropertiesModel());
        final VersionedCertificateEntityId certificateEntityId = certificateVaultFake
                .importCertificateVersion(certificateName, toCertificateImportInput(certificateName, request));
        certificateVaultFake.addTags(certificateEntityId, request.getTags());
        //no need to set expiry, the generation should take care of it based on the X509 properties
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

    private CertificateImportInput toCertificateImportInput(
            final String certificateName, final CertificateImportRequest request) {
        final CertificatePolicyModel policyModel = Objects.requireNonNullElse(request.getPolicy(), new CertificatePolicyModel());
        return new CertificateImportInput(
                certificateName,
                request.getCertificateAsString(),
                request.getPassword(),
                determineContentType(request),
                policyModel);
    }

    private CertContentType determineContentType(final CertificateImportRequest request) {
        CertContentType parsed = CertContentType.PKCS12;
        if (request.getCertificateAsString().contains("BEGIN")) {
            parsed = CertContentType.PEM;
        }
        if (request.getPolicy() != null
                && request.getPolicy().getSecretProperties() != null
                && request.getPolicy().getSecretProperties().getContentType() != null) {
            final String contentType = request.getPolicy().getSecretProperties().getContentType();
            Assert.isTrue(parsed.getMimeType().equals(contentType), "Content type must match certificate content when provided.");
        }
        return parsed;
    }
}
