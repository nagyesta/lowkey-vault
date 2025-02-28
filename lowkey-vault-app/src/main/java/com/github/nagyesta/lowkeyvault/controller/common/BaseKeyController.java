package com.github.nagyesta.lowkeyvault.controller.common;

import com.github.nagyesta.lowkeyvault.mapper.common.registry.KeyConverterRegistry;
import com.github.nagyesta.lowkeyvault.mapper.v7_2.key.KeyEntityToV72KeyItemModelConverter;
import com.github.nagyesta.lowkeyvault.mapper.v7_2.key.KeyEntityToV72KeyVersionItemModelConverter;
import com.github.nagyesta.lowkeyvault.mapper.v7_2.key.KeyEntityToV72ModelConverter;
import com.github.nagyesta.lowkeyvault.model.common.backup.KeyBackupList;
import com.github.nagyesta.lowkeyvault.model.common.backup.KeyBackupListItem;
import com.github.nagyesta.lowkeyvault.model.common.backup.KeyBackupModel;
import com.github.nagyesta.lowkeyvault.model.v7_2.BasePropertiesUpdateModel;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.*;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.request.CreateKeyRequest;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.request.ImportKeyRequest;
import com.github.nagyesta.lowkeyvault.service.key.KeyVaultFake;
import com.github.nagyesta.lowkeyvault.service.key.ReadOnlyKeyVaultKeyEntity;
import com.github.nagyesta.lowkeyvault.service.key.id.KeyEntityId;
import com.github.nagyesta.lowkeyvault.service.key.id.VersionedKeyEntityId;
import com.github.nagyesta.lowkeyvault.service.key.impl.KeyCreateDetailedInput;
import com.github.nagyesta.lowkeyvault.service.key.impl.KeyImportInput;
import com.github.nagyesta.lowkeyvault.service.vault.VaultFake;
import com.github.nagyesta.lowkeyvault.service.vault.VaultService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;

import java.util.Objects;

@Slf4j
public abstract class BaseKeyController extends GenericEntityController<KeyEntityId, VersionedKeyEntityId, ReadOnlyKeyVaultKeyEntity,
        KeyVaultKeyModel, DeletedKeyVaultKeyModel, KeyVaultKeyItemModel, DeletedKeyVaultKeyItemModel,
        KeyEntityToV72ModelConverter, KeyEntityToV72KeyItemModelConverter, KeyEntityToV72KeyVersionItemModelConverter,
        KeyVaultFake, KeyPropertiesModel, KeyBackupListItem, KeyBackupList, KeyBackupModel, KeyConverterRegistry> {

    protected BaseKeyController(@NonNull final KeyConverterRegistry registry,
                                @NonNull final VaultService vaultService) {
        super(registry, vaultService, VaultFake::keyVaultFake);
    }

    protected VersionedKeyEntityId createKeyWithAttributes(
            final KeyVaultFake keyVaultFake, final String keyName, final CreateKeyRequest request) {
        final var properties = Objects.requireNonNullElse(request.getProperties(), new KeyPropertiesModel());
        //no need to set managed property as this endpoint cannot create managed entities by definition
        return keyVaultFake.createKeyVersion(keyName, KeyCreateDetailedInput.builder()
                .key(request.toKeyCreationInput())
                .keyOperations(request.getKeyOperations())
                .tags(request.getTags())
                .expiresOn(properties.getExpiresOn())
                .notBefore(properties.getNotBefore())
                .enabled(properties.isEnabled())
                .hsm(request.getKeyType().isHsm())
                .managed(false)
                .build());
    }

    protected VersionedKeyEntityId importKeyWithAttributes(
            final KeyVaultFake keyVaultFake, final String keyName, final ImportKeyRequest request) {
        final var properties = Objects
                .requireNonNullElse(request.getProperties(), new BasePropertiesUpdateModel());
        return keyVaultFake.importKeyVersion(keyName, KeyImportInput.builder()
                .key(request.getKey())
                .hsm(request.getHsm())
                .tags(request.getTags())
                .enabled(properties.getEnabled())
                .expiresOn(properties.getExpiresOn())
                .notBefore(properties.getNotBefore())
                .createdOn(null)
                .updatedOn(null)
                .managed(false)
                .build());
    }
}
