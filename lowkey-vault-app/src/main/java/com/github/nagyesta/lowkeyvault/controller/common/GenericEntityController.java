package com.github.nagyesta.lowkeyvault.controller.common;

import com.github.nagyesta.lowkeyvault.mapper.common.RecoveryAwareConverter;
import com.github.nagyesta.lowkeyvault.mapper.common.RecoveryAwareItemConverter;
import com.github.nagyesta.lowkeyvault.model.common.KeyVaultItemListModel;
import com.github.nagyesta.lowkeyvault.model.v7_2.BasePropertiesUpdateModel;
import com.github.nagyesta.lowkeyvault.service.EntityId;
import com.github.nagyesta.lowkeyvault.service.common.BaseVaultEntity;
import com.github.nagyesta.lowkeyvault.service.common.BaseVaultFake;
import com.github.nagyesta.lowkeyvault.service.exception.NotFoundException;
import com.github.nagyesta.lowkeyvault.service.vault.VaultFake;
import com.github.nagyesta.lowkeyvault.service.vault.VaultService;
import org.jspecify.annotations.Nullable;

import java.net.URI;
import java.util.*;
import java.util.function.Function;

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
 * @param <S>   The fake type holding the entities.
 */
@SuppressWarnings("java:S119") //It is easier to ensure that the types are consistent this way
public abstract class GenericEntityController<
        K extends EntityId, V extends K, E extends BaseVaultEntity<V>,
        M, DM extends M, I, DI extends I, S extends BaseVaultFake<K, V, E>>
        extends BaseEntityReadController<K, V, E, S> {

    private final RecoveryAwareConverter<E, M, DM> modelConverter;
    private final RecoveryAwareItemConverter<E, I, DI> itemConverter;

    protected GenericEntityController(
            final VaultService vaultService,
            final RecoveryAwareConverter<E, M, DM> modelConverter,
            final RecoveryAwareItemConverter<E, I, DI> itemConverter,
            final Function<VaultFake, S> toEntityVault) {
        super(vaultService, toEntityVault);
        this.modelConverter = modelConverter;
        this.itemConverter = itemConverter;
    }

    protected M getModelById(
            final S entityVaultFake,
            final V entityId,
            final URI baseUri,
            final boolean includeDisabled) {
        final var entity = entityVaultFake.getEntities().getReadOnlyEntity(entityId);
        if (!includeDisabled && !entity.isEnabled()) {
            throw new NotFoundException("Operation get is not allowed on a disabled entity.");
        }
        return Objects.requireNonNull(modelConverter.convert(entity, baseUri));
    }

    protected DM getDeletedModelById(
            final S entityVaultFake,
            final V entityId,
            final URI baseUri,
            final boolean includeDisabled) {
        final var entity = entityVaultFake.getDeletedEntities().getReadOnlyEntity(entityId);
        if (!includeDisabled && !entity.isEnabled()) {
            throw new NotFoundException("Operation get is not allowed on a disabled entity.");
        }
        return Objects.requireNonNull(modelConverter.convertDeleted(entity, baseUri));
    }

    protected M convertDetails(
            final E entity,
            final URI vaultUri) {
        return Objects.requireNonNull(modelConverter.convert(entity, vaultUri));
    }

    protected KeyVaultItemListModel<I> getPageOfItemVersions(
            final URI baseUri,
            final String name,
            final PaginationContext pagination) {
        final var entityVaultFake = getVaultByUri(baseUri);
        final var entityId = entityId(baseUri, name);
        final var allItems = entityVaultFake.getEntities().getVersions(entityId)
                .stream()
                .sorted()
                .toList();
        final var items = filterList(pagination.getLimit(), pagination.getOffset(), allItems, v -> {
            final var entity = getEntityByNameAndVersion(baseUri, name, v);
            return Objects.requireNonNull(itemConverter.convert(entity, baseUri));
        });
        final var nextUri = PaginationContext.builder()
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
    protected KeyVaultItemListModel<I> getPageOfItems(
            final URI baseUri,
            final PaginationContext pagination) {
        final var entityVaultFake = getVaultByUri(baseUri);
        final var allItems = entityVaultFake.getEntities().listLatestEntities();
        final var items = filterList(pagination.getLimit(), pagination.getOffset(), allItems,
                source -> Objects.requireNonNull(itemConverter.convertWithoutVersion(source, baseUri)));
        final var nextUri = PaginationContext.builder()
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
    protected KeyVaultItemListModel<DI> getPageOfDeletedItems(
            final URI baseUri,
            final PaginationContext pagination) {
        final var entityVaultFake = getVaultByUri(baseUri);
        final var allItems = entityVaultFake.getDeletedEntities().listLatestEntities();
        final var items = filterList(pagination.getLimit(), pagination.getOffset(), allItems,
                source -> Objects.requireNonNull(itemConverter.convertDeleted(source, baseUri)));
        final var nextUri = PaginationContext.builder()
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

    protected M getLatestEntityModel(
            final URI baseUri,
            final String name) {
        final var vaultFake = getVaultByUri(baseUri);
        final var entityId = vaultFake.getEntities().getLatestVersionOfEntity(entityId(baseUri, name));
        return getModelById(vaultFake, entityId, baseUri, false);
    }

    protected M getSpecificEntityModel(
            final URI baseUri,
            final String name,
            final String version) {
        final var vaultFake = getVaultByUri(baseUri);
        final var entityId = versionedEntityId(baseUri, name, version);
        return getModelById(vaultFake, entityId, baseUri, false);
    }

    protected void updateAttributes(
            final BaseVaultFake<K, V, ?> vaultFake,
            final V entityId,
            @Nullable final BasePropertiesUpdateModel properties) {
        Optional.ofNullable(properties)
                .ifPresent(attributes -> {
                    Optional.ofNullable(attributes.getEnabled())
                            .ifPresent(enabled -> vaultFake.setEnabled(entityId, enabled));
                    if (attributes.getExpiresOn() != null || attributes.getNotBefore() != null) {
                        vaultFake.setExpiry(entityId, attributes.getNotBefore(), attributes.getExpiresOn());
                    }
                });
    }

    protected void updateTags(
            final BaseVaultFake<K, V, ?> vaultFake,
            final V entityId,
            @Nullable final Map<String, String> requestTags) {
        Optional.ofNullable(requestTags)
                .ifPresent(tags -> {
                    vaultFake.clearTags(entityId);
                    vaultFake.addTags(entityId, tags);
                });
    }

    protected <LI> KeyVaultItemListModel<LI> listModel(
            final List<LI> items,
            @Nullable final URI nextUri) {
        return new KeyVaultItemListModel<>(items, nextUri);
    }

    private <FR, LI> List<LI> filterList(
            final int limit,
            final int offset,
            final Collection<FR> allItems,
            final Function<FR, LI> mapper) {
        return allItems.stream()
                .skip(offset)
                .limit(limit)
                .map(mapper)
                .toList();
    }

}
