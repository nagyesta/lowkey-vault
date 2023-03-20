package com.github.nagyesta.lowkeyvault.controller.common;

import com.github.nagyesta.lowkeyvault.mapper.common.registry.CertificateConverterRegistry;
import com.github.nagyesta.lowkeyvault.mapper.v7_3.certificate.CertificateEntityToV73CertificateItemModelConverter;
import com.github.nagyesta.lowkeyvault.mapper.v7_3.certificate.CertificateEntityToV73CertificateVersionItemModelConverter;
import com.github.nagyesta.lowkeyvault.mapper.v7_3.certificate.CertificateEntityToV73ModelConverter;
import com.github.nagyesta.lowkeyvault.model.common.backup.CertificateBackupList;
import com.github.nagyesta.lowkeyvault.model.common.backup.CertificateBackupListItem;
import com.github.nagyesta.lowkeyvault.model.common.backup.CertificateBackupModel;
import com.github.nagyesta.lowkeyvault.model.v7_3.certificate.*;
import com.github.nagyesta.lowkeyvault.service.certificate.CertificateVaultFake;
import com.github.nagyesta.lowkeyvault.service.certificate.ReadOnlyKeyVaultCertificateEntity;
import com.github.nagyesta.lowkeyvault.service.certificate.id.CertificateEntityId;
import com.github.nagyesta.lowkeyvault.service.certificate.id.VersionedCertificateEntityId;
import com.github.nagyesta.lowkeyvault.service.vault.VaultFake;
import com.github.nagyesta.lowkeyvault.service.vault.VaultService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;

import java.net.URI;
import java.util.Optional;

@Slf4j
public abstract class BaseCertificateController extends GenericEntityController<CertificateEntityId, VersionedCertificateEntityId,
        ReadOnlyKeyVaultCertificateEntity, KeyVaultCertificateModel, DeletedKeyVaultCertificateModel, KeyVaultCertificateItemModel,
        DeletedKeyVaultCertificateItemModel, CertificateEntityToV73ModelConverter, CertificateEntityToV73CertificateItemModelConverter,
        CertificateEntityToV73CertificateVersionItemModelConverter, CertificateVaultFake, CertificatePropertiesModel,
        CertificateBackupListItem, CertificateBackupList, CertificateBackupModel, CertificateConverterRegistry> {

    /**
     * Default parameter value for including the pending certificates.
     */
    protected static final String TRUE = "true";
    /**
     * Parameter name for including the pending certificates.
     */
    protected static final String INCLUDE_PENDING_PARAM = "includePending";

    protected BaseCertificateController(
            @NonNull final CertificateConverterRegistry registry,
            @NonNull final VaultService vaultService) {
        super(registry, vaultService, VaultFake::certificateVaultFake);
    }

    @Override
    protected KeyVaultCertificateModel getModelById(
            final CertificateVaultFake entityVaultFake,
            final VersionedCertificateEntityId entityId,
            final URI baseUri,
            final boolean includeDisabled) {
        final KeyVaultCertificateModel model = super.getModelById(entityVaultFake, entityId, baseUri, includeDisabled);
        populateLifetimeActions(entityVaultFake, entityId, model.getPolicy());
        return model;
    }

    @Override
    protected DeletedKeyVaultCertificateModel getDeletedModelById(
            final CertificateVaultFake entityVaultFake,
            final VersionedCertificateEntityId entityId,
            final URI baseUri,
            final boolean includeDisabled) {
        final DeletedKeyVaultCertificateModel model = super.getDeletedModelById(entityVaultFake, entityId, baseUri, includeDisabled);
        populateLifetimeActions(entityVaultFake, entityId, model.getPolicy());
        return model;
    }

    protected void populateLifetimeActions(
            final CertificateVaultFake vaultFake, final VersionedCertificateEntityId entityId, final CertificatePolicyModel model) {
        Optional.ofNullable(vaultFake.lifetimeActionPolicy(entityId))
                .map(registry().lifetimeActionConverters(apiVersion())::convert)
                .ifPresent(model::setLifetimeActions);
    }
}
