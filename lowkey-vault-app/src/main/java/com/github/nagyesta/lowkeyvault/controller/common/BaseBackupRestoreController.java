package com.github.nagyesta.lowkeyvault.controller.common;

import com.github.nagyesta.lowkeyvault.mapper.common.RecoveryAwareConverter;
import com.github.nagyesta.lowkeyvault.mapper.common.RecoveryAwareItemConverter;
import com.github.nagyesta.lowkeyvault.model.v7_2.BasePropertiesModel;
import com.github.nagyesta.lowkeyvault.model.v7_2.common.BaseBackupListItem;
import com.github.nagyesta.lowkeyvault.model.v7_2.common.BaseBackupModel;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.BackupListContainer;
import com.github.nagyesta.lowkeyvault.service.EntityId;
import com.github.nagyesta.lowkeyvault.service.common.BaseVaultEntity;
import com.github.nagyesta.lowkeyvault.service.common.BaseVaultFake;
import com.github.nagyesta.lowkeyvault.service.vault.VaultFake;
import com.github.nagyesta.lowkeyvault.service.vault.VaultService;
import org.jspecify.annotations.Nullable;
import org.springframework.util.Assert;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * The base implementation of the backup/restore controllers.
 *
 * @param <K>   The type of the key (not versioned).
 * @param <V>   The versioned key type.
 * @param <E>   The entity type.
 * @param <M>   The entity model type.
 * @param <DM>  The deleted entity model type.
 * @param <I>   The item model type.
 * @param <DI>  The deleted item model type.
 * @param <S>   The fake type holding the entities.
 * @param <PM>  The type of the PropertiesModel.
 * @param <BLI> The list item type of the backup lists.
 * @param <BL>  The type of the backup list.
 * @param <B>   The type of the backup model.
 */
@SuppressWarnings("java:S119") //It is easier to ensure that the types are consistent this way
public abstract class BaseBackupRestoreController<K extends EntityId, V extends K, E extends BaseVaultEntity<V>,
        M, DM extends M, I, DI extends I,
        S extends BaseVaultFake<K, V, E>,
        PM extends BasePropertiesModel,
        BLI extends BaseBackupListItem<PM>,
        BL extends BackupListContainer<BLI>,
        B extends BaseBackupModel<PM, BLI, BL>>
        extends GenericEntityController<K, V, E, M, DM, I, DI, S> {

    private final RecoveryAwareConverter<E, M, DM> modelConverter;
    private final Function<E, BLI> convertBackup;

    protected BaseBackupRestoreController(
            final VaultService vaultService,
            final RecoveryAwareConverter<E, M, DM> modelConverter,
            final RecoveryAwareItemConverter<E, I, DI> itemConverter,
            final Function<VaultFake, S> toEntityVault,
            final Function<E, BLI> convertBackup) {
        super(vaultService, modelConverter, itemConverter, toEntityVault);
        this.modelConverter = modelConverter;
        this.convertBackup = convertBackup;
    }

    protected M restoreEntity(final B backupModel) {
        final var baseUri = getSingleBaseUri(backupModel);
        final var vault = getVaultByUri(baseUri);
        final var id = getSingleEntityName(backupModel);
        final var entityId = entityId(baseUri, id);
        assertNameDoesNotExistYet(vault, entityId);
        backupModel.getValue().getVersions().forEach(entityVersion -> {
            final var versionedEntityId = versionedEntityId(baseUri, id, entityVersion.getVersion());
            restoreVersion(vault, versionedEntityId, entityVersion);
        });
        final var latestVersionOfEntity = vault.getEntities().getLatestVersionOfEntity(entityId);
        final var readOnlyEntity = vault.getEntities().getReadOnlyEntity(latestVersionOfEntity);
        return Objects.requireNonNull(modelConverter.convert(readOnlyEntity, baseUri));
    }

    protected abstract void restoreVersion(S vault, V versionedEntityId, BLI entityVersion);

    protected B backupEntity(final K entityId) {
        final var entities = getVaultByUri(entityId.vault())
                .getEntities();
        final var list = entities.getVersions(entityId).stream()
                .map(version -> getEntityByNameAndVersion(entityId.vault(), entityId.id(), version))
                .map(convertBackup)
                .toList();
        return wrapBackup(list);
    }

    protected abstract B getBackupModel();

    protected abstract BL getBackupList();

    protected String getSingleEntityName(final B backupModel) {
        final var entityNames = backupModel.getValue().getVersions().stream()
                .map(BLI::getId)
                .distinct()
                .toList();
        Assert.isTrue(entityNames.size() == 1, "All backup entities must belong to the same entity.");
        return entityNames.getFirst();
    }

    protected URI getSingleBaseUri(final B backupModel) {
        final var uris = backupModel.getValue().getVersions().stream()
                .map(BLI::getVaultBaseUri)
                .distinct()
                .toList();
        Assert.isTrue(uris.size() == 1, "All backup entities must be from the same vault.");
        return uris.getFirst();
    }

    private B wrapBackup(@Nullable final List<BLI> list) {
        final var listModel = Optional.ofNullable(list)
                .map(l -> {
                    final var backupList = getBackupList();
                    backupList.setVersions(l);
                    return backupList;
                })
                .orElse(getBackupList());
        final var backupModel = getBackupModel();
        backupModel.setValue(listModel);
        return backupModel;
    }

    private void assertNameDoesNotExistYet(
            final S vault,
            final K entityId) {
        Assert.isTrue(!vault.getEntities().containsName(entityId.id()),
                "Vault already contains entity with name: " + entityId.id());
        Assert.isTrue(!vault.getDeletedEntities().containsName(entityId.id()),
                "Vault already contains deleted entity with name: " + entityId.id());
    }
}
