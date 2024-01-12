package com.github.nagyesta.lowkeyvault.controller.common;

import com.github.nagyesta.lowkeyvault.mapper.common.BaseEntityConverterRegistry;
import com.github.nagyesta.lowkeyvault.mapper.common.RecoveryAwareConverter;
import com.github.nagyesta.lowkeyvault.model.common.KeyVaultItemListModel;
import com.github.nagyesta.lowkeyvault.model.v7_2.BasePropertiesModel;
import com.github.nagyesta.lowkeyvault.model.v7_2.BasePropertiesUpdateModel;
import com.github.nagyesta.lowkeyvault.model.v7_2.common.BaseBackupListItem;
import com.github.nagyesta.lowkeyvault.model.v7_2.common.BaseBackupModel;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.BackupListContainer;
import com.github.nagyesta.lowkeyvault.service.EntityId;
import com.github.nagyesta.lowkeyvault.service.common.BaseVaultEntity;
import com.github.nagyesta.lowkeyvault.service.common.BaseVaultFake;
import com.github.nagyesta.lowkeyvault.service.exception.NotFoundException;
import com.github.nagyesta.lowkeyvault.service.vault.VaultFake;
import com.github.nagyesta.lowkeyvault.service.vault.VaultService;
import lombok.NonNull;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * The base implementation of the entity controllers.
 *
 * @param <K>   The type of the key (not versioned).
 * @param <V>   The versioned key type.
 * @param <E>   The entity type.
 * @param <M>   The entity model type.
 * @param <DM>  The deleted entity model type.
 * @param <I>   The item model type.
 * @param <DI>  The deleted item model type.
 * @param <MC>  The model converter, converting entities to entity models.
 * @param <IC>  The item converter, converting version item entities to item models.
 * @param <VIC> The versioned item converter, converting version item entities to item models.
 * @param <S>   The fake type holding the entities.
 * @param <PM>  The type of the PropertiesModel.
 * @param <BLI> The list item type of the backup lists.
 * @param <BL>  The type of the backup list.
 * @param <B>   The type of the backup model.
 * @param <R>   The ConverterRegistry used for conversions.
 */
public abstract class GenericEntityController<K extends EntityId, V extends K, E extends BaseVaultEntity<V>,
        M, DM extends M, I, DI extends I,
        MC extends RecoveryAwareConverter<E, M, DM>,
        IC extends RecoveryAwareConverter<E, I, DI>,
        VIC extends RecoveryAwareConverter<E, I, DI>,
        S extends BaseVaultFake<K, V, E>,
        PM extends BasePropertiesModel,
        BLI extends BaseBackupListItem<PM>,
        BL extends BackupListContainer<BLI>,
        B extends BaseBackupModel<PM, BLI, BL>,
        R extends BaseEntityConverterRegistry<K, V, E, M, DM, PM, I, DI, BLI, BL, B>> extends BaseEntityReadController<K, V, E, S> {
    /**
     * Default page size used when returning available versions of an entity.
     */
    protected static final String DEFAULT_MAX = "25";
    /**
     * Default offset used  when returning available versions of an entity.
     */
    protected static final String SKIP_ZERO = "0";
    /**
     * Parameter name for the page size when returning versions of an entity.
     */
    protected static final String MAX_RESULTS_PARAM = "maxresults";
    /**
     * Parameter name for the offset when returning versions of an entity.
     */
    protected static final String SKIP_TOKEN_PARAM = "$skiptoken";
    private final R registry;

    protected R registry() {
        return registry;
    }

    protected GenericEntityController(@NonNull final R registry,
                                      @org.springframework.lang.NonNull final VaultService vaultService,
                                      @org.springframework.lang.NonNull final Function<VaultFake, S> toEntityVault) {
        super(vaultService, toEntityVault);
        this.registry = registry;
    }

    @Override
    protected final V versionedEntityId(final URI baseUri, final String name, final String version) {
        return registry.versionedEntityId(baseUri, name, version);
    }

    @Override
    protected final K entityId(final URI baseUri, final String name) {
        return registry.entityId(baseUri, name);
    }

    protected M getModelById(final S entityVaultFake, final V entityId, final URI baseUri, final boolean includeDisabled) {
        final E entity = entityVaultFake.getEntities().getReadOnlyEntity(entityId);
        if (!includeDisabled && !entity.isEnabled()) {
            throw new NotFoundException("Operation get is not allowed on a disabled entity.");
        }
        return registry.modelConverter(apiVersion()).convert(entity, baseUri);
    }

    protected DM getDeletedModelById(final S entityVaultFake, final V entityId, final URI baseUri, final boolean includeDisabled) {
        final E entity = entityVaultFake.getDeletedEntities().getReadOnlyEntity(entityId);
        if (!includeDisabled && !entity.isEnabled()) {
            throw new NotFoundException("Operation get is not allowed on a disabled entity.");
        }
        return registry.modelConverter(apiVersion()).convertDeleted(entity, baseUri);
    }

    protected M convertDetails(final E entity, final URI vaultUri) {
        return registry.modelConverter(apiVersion()).convert(entity, vaultUri);
    }

    protected KeyVaultItemListModel<I> getPageOfItemVersions(
            final URI baseUri, final String name, final PaginationContext pagination) {
        final S entityVaultFake = getVaultByUri(baseUri);
        final K entityId = entityId(baseUri, name);
        final List<String> allItems = entityVaultFake.getEntities().getVersions(entityId)
                .stream()
                .sorted()
                .collect(Collectors.toList());
        final List<I> items = filterList(pagination.getLimit(), pagination.getOffset(), allItems, v -> {
            final E entity = getEntityByNameAndVersion(baseUri, name, v);
            return registry.versionedItemConverter(apiVersion()).convert(entity, baseUri);
        });
        final URI nextUri = PaginationContext.builder()
                .base(pagination.getBase())
                .apiVersion(pagination.getApiVersion())
                .currentItems(items.size())
                .totalItems(allItems.size())
                .limit(pagination.getLimit())
                .offset(pagination.getOffset())
                .additionalParameters(pagination.getAdditionalParameters())
                .build()
                .asNextUri();
        return listModel(items, nextUri);
    }

    @SuppressWarnings("SameParameterValue")
    protected KeyVaultItemListModel<I> getPageOfItems(final URI baseUri, final PaginationContext pagination) {
        final S entityVaultFake = getVaultByUri(baseUri);
        final List<E> allItems = entityVaultFake.getEntities().listLatestEntities();
        final List<I> items = filterList(pagination.getLimit(), pagination.getOffset(), allItems,
                source -> registry.itemConverter(apiVersion()).convert(source, baseUri));
        final URI nextUri = PaginationContext.builder()
                .base(pagination.getBase())
                .apiVersion(pagination.getApiVersion())
                .currentItems(items.size())
                .totalItems(allItems.size())
                .limit(pagination.getLimit())
                .offset(pagination.getOffset())
                .additionalParameters(pagination.getAdditionalParameters())
                .build()
                .asNextUri();
        return listModel(items, nextUri);
    }

    @SuppressWarnings("SameParameterValue")
    protected KeyVaultItemListModel<DI> getPageOfDeletedItems(final URI baseUri, final PaginationContext pagination) {
        final S entityVaultFake = getVaultByUri(baseUri);
        final List<E> allItems = entityVaultFake.getDeletedEntities().listLatestEntities();
        final List<DI> items = filterList(pagination.getLimit(), pagination.getOffset(), allItems,
                source -> registry.itemConverter(apiVersion()).convertDeleted(source, baseUri));
        final URI nextUri = PaginationContext.builder()
                .base(pagination.getBase())
                .apiVersion(apiVersion())
                .currentItems(items.size())
                .totalItems(allItems.size())
                .limit(pagination.getLimit())
                .offset(pagination.getOffset())
                .additionalParameters(pagination.getAdditionalParameters())
                .build()
                .asNextUri();
        return listModel(items, nextUri);
    }

    protected M getLatestEntityModel(final URI baseUri, final String name) {
        final S vaultFake = getVaultByUri(baseUri);
        final V entityId = vaultFake.getEntities().getLatestVersionOfEntity(entityId(baseUri, name));
        return getModelById(vaultFake, entityId, baseUri, false);
    }

    protected M getSpecificEntityModel(final URI baseUri, final String name, final String version) {
        final S vaultFake = getVaultByUri(baseUri);
        final V entityId = versionedEntityId(baseUri, name, version);
        return getModelById(vaultFake, entityId, baseUri, false);
    }

    protected void updateAttributes(final BaseVaultFake<K, V, ?> vaultFake, final V entityId, final BasePropertiesUpdateModel properties) {
        Optional.ofNullable(properties)
                .ifPresent(attributes -> {
                    Optional.ofNullable(attributes.getEnabled())
                            .ifPresent(enabled -> vaultFake.setEnabled(entityId, enabled));
                    if (attributes.getExpiresOn() != null || attributes.getNotBefore() != null) {
                        vaultFake.setExpiry(entityId, attributes.getNotBefore(), attributes.getExpiresOn());
                    }
                });
    }

    protected void updateTags(final BaseVaultFake<K, V, ?> vaultFake, final V entityId, final Map<String, String> requestTags) {
        Optional.ofNullable(requestTags)
                .ifPresent(tags -> {
                    vaultFake.clearTags(entityId);
                    vaultFake.addTags(entityId, tags);
                });
    }

    protected <LI> KeyVaultItemListModel<LI> listModel(final List<LI> items, final URI nextUri) {
        return new KeyVaultItemListModel<>(items, nextUri);
    }

    private <FR, LI> List<LI> filterList(
            final int limit, final int offset, final Collection<FR> allItems, final Function<FR, LI> mapper) {
        return allItems.stream()
                .skip(offset)
                .limit(limit)
                .map(mapper)
                .collect(Collectors.toList());
    }

}
