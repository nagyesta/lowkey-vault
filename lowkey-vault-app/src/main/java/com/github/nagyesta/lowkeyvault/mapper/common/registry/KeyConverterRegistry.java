package com.github.nagyesta.lowkeyvault.mapper.common.registry;

import com.github.nagyesta.lowkeyvault.mapper.common.AliasAwareConverter;
import com.github.nagyesta.lowkeyvault.mapper.common.ApiVersionAwareConverter;
import com.github.nagyesta.lowkeyvault.mapper.common.BaseEntityConverterRegistry;
import com.github.nagyesta.lowkeyvault.model.common.backup.KeyBackupList;
import com.github.nagyesta.lowkeyvault.model.common.backup.KeyBackupListItem;
import com.github.nagyesta.lowkeyvault.model.common.backup.KeyBackupModel;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.*;
import com.github.nagyesta.lowkeyvault.model.v7_3.key.KeyRotationPolicyModel;
import com.github.nagyesta.lowkeyvault.service.key.ReadOnlyKeyVaultKeyEntity;
import com.github.nagyesta.lowkeyvault.service.key.ReadOnlyRotationPolicy;
import com.github.nagyesta.lowkeyvault.service.key.RotationPolicy;
import com.github.nagyesta.lowkeyvault.service.key.id.KeyEntityId;
import com.github.nagyesta.lowkeyvault.service.key.id.VersionedKeyEntityId;
import lombok.EqualsAndHashCode;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
public class KeyConverterRegistry extends BaseEntityConverterRegistry<KeyEntityId, VersionedKeyEntityId, ReadOnlyKeyVaultKeyEntity,
        KeyVaultKeyModel, DeletedKeyVaultKeyModel, KeyPropertiesModel, KeyVaultKeyItemModel, DeletedKeyVaultKeyItemModel,
        KeyBackupListItem, KeyBackupList, KeyBackupModel> {

    private final Map<String, AliasAwareConverter<ReadOnlyRotationPolicy, KeyRotationPolicyModel>> rotationPolicyModelConverters =
            new HashMap<>();

    private final Map<String, ApiVersionAwareConverter<KeyRotationPolicyModel, RotationPolicy>> rotationPolicyEntityConverters =
            new HashMap<>();

    @Override
    public KeyEntityId entityId(final URI baseUri, final String name) {
        return new KeyEntityId(baseUri, name);
    }

    @Override
    public VersionedKeyEntityId versionedEntityId(final URI baseUri, final String name, final String version) {
        return new VersionedKeyEntityId(baseUri, name, version);
    }

    public AliasAwareConverter<ReadOnlyRotationPolicy, KeyRotationPolicyModel> rotationPolicyModelConverter(
            final String apiVersion) {
        return rotationPolicyModelConverters.get(apiVersion);
    }

    public void registerRotationPolicyModelConverter(final AliasAwareConverter<ReadOnlyRotationPolicy, KeyRotationPolicyModel> converter) {
        converter.supportedVersions().forEach(v -> rotationPolicyModelConverters.put(v, converter));
    }

    public ApiVersionAwareConverter<KeyRotationPolicyModel, RotationPolicy> rotationPolicyEntityConverter(
            final String apiVersion) {
        return rotationPolicyEntityConverters.get(apiVersion);
    }

    public void registerRotationPolicyEntityConverter(final ApiVersionAwareConverter<KeyRotationPolicyModel, RotationPolicy> converter) {
        converter.supportedVersions().forEach(v -> rotationPolicyEntityConverters.put(v, converter));
    }
}
