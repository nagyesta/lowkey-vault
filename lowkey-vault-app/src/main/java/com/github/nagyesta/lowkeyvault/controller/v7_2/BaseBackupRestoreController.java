package com.github.nagyesta.lowkeyvault.controller.v7_2;

import com.github.nagyesta.lowkeyvault.mapper.common.BackupConverter;
import com.github.nagyesta.lowkeyvault.mapper.common.RecoveryAwareConverter;
import com.github.nagyesta.lowkeyvault.model.v7_2.BasePropertiesModel;
import com.github.nagyesta.lowkeyvault.model.v7_2.common.BaseBackupListItem;
import com.github.nagyesta.lowkeyvault.model.v7_2.common.BaseBackupModel;
import com.github.nagyesta.lowkeyvault.service.EntityId;
import com.github.nagyesta.lowkeyvault.service.common.BaseVaultEntity;
import com.github.nagyesta.lowkeyvault.service.common.BaseVaultFake;
import com.github.nagyesta.lowkeyvault.service.common.ReadOnlyVersionedEntityMultiMap;
import com.github.nagyesta.lowkeyvault.service.common.impl.KeyVaultBaseEntity;
import com.github.nagyesta.lowkeyvault.service.vault.VaultFake;
import com.github.nagyesta.lowkeyvault.service.vault.VaultService;
import lombok.NonNull;
import org.springframework.util.Assert;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * The base implementation of the backup/restore controllers.
 *
 * @param <K>   The type of the entity id (not versioned).
 * @param <V>   The versioned entity id type.
 * @param <E>   The entity type.
 * @param <M>   The entity model type.
 * @param <DM>  The deleted entity model type.
 * @param <P>   The type of the properties model.
 * @param <BLI> The type of the list item representing one entity version in the backup model.
 * @param <BL>  The wrapper type of the list in the backup model.
 * @param <B>   The type of the backup model.
 * @param <MC>  The model converter, converting entities to entity models.
 * @param <BC>  The converter, converting entities to list items of the backup models.
 * @param <S>   The fake type holding the entities.
 */
public abstract class BaseBackupRestoreController<K extends EntityId, V extends K, E extends BaseVaultEntity<V>, M, DM extends M,
        P extends BasePropertiesModel, BLI extends BaseBackupListItem<P>, BL extends List<BLI>, B extends BaseBackupModel<P, BLI, BL>,
        BC extends BackupConverter<V, E, P, BLI>, MC extends RecoveryAwareConverter<E, M, DM>,
        S extends BaseVaultFake<K, V, E>> extends BaseEntityReadController<K, V, E, S> {

    private final MC modelConverter;
    private final BC backupConverter;

    protected BaseBackupRestoreController(@NonNull final MC modelConverter,
                                          @NonNull final BC backupConverter,
                                          @org.springframework.lang.NonNull final VaultService vaultService,
                                          @org.springframework.lang.NonNull final Function<VaultFake, S> toEntityVault) {
        super(vaultService, toEntityVault);
        this.modelConverter = modelConverter;
        this.backupConverter = backupConverter;
    }

    protected M restoreEntity(final B backupModel) {
        final URI baseUri = getSingleBaseUri(backupModel);
        final S vault = getVaultByUri(baseUri);
        final String id = getSingleEntityName(backupModel);
        final K entityId = entityId(baseUri, id);
        assertNameDoesNotExistYet(vault, entityId);
        backupModel.getValue().forEach(entityVersion -> {
            final V versionedEntityId = versionedEntityId(baseUri, id, entityVersion.getVersion());
            restoreVersion(vault, versionedEntityId, entityVersion);
        });
        final V latestVersionOfEntity = vault.getEntities().getLatestVersionOfEntity(entityId);
        final E readOnlyEntity = vault.getEntities().getReadOnlyEntity(latestVersionOfEntity);
        return modelConverter.convert(readOnlyEntity);
    }

    protected abstract void restoreVersion(S vault, V versionedEntityId, BLI entityVersion);

    protected void updateCommonFields(final BLI entityVersion, final KeyVaultBaseEntity<V> entity) {
        final P attributes = entityVersion.getAttributes();
        entity.setTags(Objects.requireNonNullElse(entityVersion.getTags(), Map.of()));
        entity.setExpiry(attributes.getExpiresOn());
        entity.setEnabled(attributes.isEnabled());
        entity.setNotBefore(attributes.getNotBefore());
        entity.setManaged(entityVersion.isManaged());
        entity.setCreatedOn(attributes.getCreatedOn());
        entity.setUpdatedOn(attributes.getUpdatedOn());
    }

    protected B backupEntity(final K entityId) {
        final ReadOnlyVersionedEntityMultiMap<K, V, E> entities = getVaultByUri(entityId.vault())
                .getEntities();
        final List<BLI> list = entities.getVersions(entityId).stream()
                .map(version -> getEntityByNameAndVersion(entityId.vault(), entityId.id(), version))
                .map(backupConverter::convert)
                .collect(Collectors.toUnmodifiableList());
        return wrapBackup(list);
    }

    protected abstract B getBackupModel();

    protected abstract BL getBackupList();

    private B wrapBackup(final List<BLI> list) {
        final BL listModel = Optional.ofNullable(list)
                .map(l -> {
                    final BL backupList = getBackupList();
                    backupList.addAll(l);
                    return backupList;
                })
                .orElse(null);
        final B backupModel = getBackupModel();
        backupModel.setValue(listModel);
        return backupModel;
    }

    private void assertNameDoesNotExistYet(final S vault, final K entityId) {
        Assert.isTrue(!vault.getEntities().containsName(entityId.id()),
                "Vault already contains entity with name: " + entityId.id());
        Assert.isTrue(!vault.getDeletedEntities().containsName(entityId.id()),
                "Vault already contains deleted entity with name: " + entityId.id());
    }

    private String getSingleEntityName(final B backupModel) {
        final List<String> entityNames = backupModel.getValue().stream()
                .map(BLI::getId)
                .distinct()
                .collect(Collectors.toUnmodifiableList());
        Assert.isTrue(entityNames.size() == 1, "All backup entities must belong to the same entity.");
        return entityNames.get(0);
    }

    private URI getSingleBaseUri(final B backupModel) {
        final List<URI> uris = backupModel.getValue().stream()
                .map(BLI::getVaultBaseUri)
                .distinct()
                .collect(Collectors.toUnmodifiableList());
        Assert.isTrue(uris.size() == 1, "All backup entities must be from the same vault.");
        return uris.get(0);
    }

}
