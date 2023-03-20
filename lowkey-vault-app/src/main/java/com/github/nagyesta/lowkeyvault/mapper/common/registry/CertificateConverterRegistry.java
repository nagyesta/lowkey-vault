package com.github.nagyesta.lowkeyvault.mapper.common.registry;

import com.github.nagyesta.lowkeyvault.mapper.common.AliasAwareConverter;
import com.github.nagyesta.lowkeyvault.mapper.common.ApiVersionAwareConverter;
import com.github.nagyesta.lowkeyvault.mapper.common.BaseEntityConverterRegistry;
import com.github.nagyesta.lowkeyvault.model.common.backup.CertificateBackupList;
import com.github.nagyesta.lowkeyvault.model.common.backup.CertificateBackupListItem;
import com.github.nagyesta.lowkeyvault.model.common.backup.CertificateBackupModel;
import com.github.nagyesta.lowkeyvault.model.v7_3.certificate.*;
import com.github.nagyesta.lowkeyvault.service.certificate.LifetimeActionPolicy;
import com.github.nagyesta.lowkeyvault.service.certificate.ReadOnlyKeyVaultCertificateEntity;
import com.github.nagyesta.lowkeyvault.service.certificate.id.CertificateEntityId;
import com.github.nagyesta.lowkeyvault.service.certificate.id.VersionedCertificateEntityId;
import lombok.EqualsAndHashCode;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
public class CertificateConverterRegistry extends BaseEntityConverterRegistry<CertificateEntityId, VersionedCertificateEntityId,
        ReadOnlyKeyVaultCertificateEntity, KeyVaultCertificateModel, DeletedKeyVaultCertificateModel, CertificatePropertiesModel,
        KeyVaultCertificateItemModel, DeletedKeyVaultCertificateItemModel, CertificateBackupListItem, CertificateBackupList,
        CertificateBackupModel> {

    private final Map<String, AliasAwareConverter<ReadOnlyKeyVaultCertificateEntity, CertificatePolicyModel>>
            policyConverters = new HashMap<>();
    private final Map<String, AliasAwareConverter<ReadOnlyKeyVaultCertificateEntity, CertificatePolicyModel>>
            issuancePolicyConverters = new HashMap<>();
    private final Map<String, AliasAwareConverter<ReadOnlyKeyVaultCertificateEntity, KeyVaultPendingCertificateModel>>
            pendingOperationConverters = new HashMap<>();
    private final Map<String, ApiVersionAwareConverter<LifetimeActionPolicy, List<CertificateLifetimeActionModel>>>
            lifetimeActionConverters = new HashMap<>();

    @Override
    public CertificateEntityId entityId(final URI baseUri, final String name) {
        return new CertificateEntityId(baseUri, name);
    }

    @Override
    public VersionedCertificateEntityId versionedEntityId(final URI baseUri, final String name, final String version) {
        return new VersionedCertificateEntityId(baseUri, name, version);
    }

    public AliasAwareConverter<ReadOnlyKeyVaultCertificateEntity, CertificatePolicyModel> policyConverters(
            final String apiVersion) {
        return policyConverters.get(apiVersion);
    }

    public void registerPolicyConverter(
            final AliasAwareConverter<ReadOnlyKeyVaultCertificateEntity, CertificatePolicyModel> converter) {
        converter.supportedVersions().forEach(v -> policyConverters.put(v, converter));
    }

    public AliasAwareConverter<ReadOnlyKeyVaultCertificateEntity, CertificatePolicyModel> issuancePolicyConverters(
            final String apiVersion) {
        return issuancePolicyConverters.get(apiVersion);
    }

    public void registerIssuancePolicyConverter(
            final AliasAwareConverter<ReadOnlyKeyVaultCertificateEntity, CertificatePolicyModel> converter) {
        converter.supportedVersions().forEach(v -> issuancePolicyConverters.put(v, converter));
    }

    public AliasAwareConverter<ReadOnlyKeyVaultCertificateEntity, KeyVaultPendingCertificateModel> pendingOperationConverters(
            final String apiVersion) {
        return pendingOperationConverters.get(apiVersion);
    }

    public void registerPendingOperationConverter(
            final AliasAwareConverter<ReadOnlyKeyVaultCertificateEntity, KeyVaultPendingCertificateModel> converter) {
        converter.supportedVersions().forEach(v -> pendingOperationConverters.put(v, converter));
    }

    public ApiVersionAwareConverter<LifetimeActionPolicy, List<CertificateLifetimeActionModel>> lifetimeActionConverters(
            final String apiVersion) {
        return lifetimeActionConverters.get(apiVersion);
    }

    public void registerLifetimeActionConverter(
            final ApiVersionAwareConverter<LifetimeActionPolicy, List<CertificateLifetimeActionModel>> converter) {
        converter.supportedVersions().forEach(v -> lifetimeActionConverters.put(v, converter));
    }
}
