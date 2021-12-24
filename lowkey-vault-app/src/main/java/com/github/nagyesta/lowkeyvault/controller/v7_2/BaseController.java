package com.github.nagyesta.lowkeyvault.controller.v7_2;

import com.github.nagyesta.lowkeyvault.exception.VaultNotFoundException;
import com.github.nagyesta.lowkeyvault.mapper.common.RecoveryAwareConverter;
import com.github.nagyesta.lowkeyvault.model.common.KeyVaultItemListModel;
import com.github.nagyesta.lowkeyvault.model.v7_2.BasePropertiesUpdateModel;
import com.github.nagyesta.lowkeyvault.service.EntityId;
import com.github.nagyesta.lowkeyvault.service.common.BaseVaultEntity;
import com.github.nagyesta.lowkeyvault.service.common.BaseVaultFake;
import com.github.nagyesta.lowkeyvault.service.vault.VaultFake;
import com.github.nagyesta.lowkeyvault.service.vault.VaultService;
import lombok.NonNull;

import java.net.URI;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.github.nagyesta.lowkeyvault.model.common.ApiConstants.V_7_2;

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
 */
public abstract class BaseController<K extends EntityId, V extends K, E extends BaseVaultEntity<V>,
        M, DM extends M, I, DI extends I, MC extends RecoveryAwareConverter<E, M, DM>,
        IC extends RecoveryAwareConverter<E, I, DI>, VIC extends RecoveryAwareConverter<E, I, DI>,
        S extends BaseVaultFake<K, V, E>> {
    /**
     * API version.
     */
    protected static final String API_VERSION_7_2 = "api-version=" + V_7_2;
    /**
     * RegExp of entity names (key name, secret name, certificate name).
     */
    protected static final String NAME_PATTERN = "^[0-9a-zA-Z-]+$";
    /**
     * RegExp of entity version identifiers (key version, secret version, certificate version).
     */
    protected static final String VERSION_NAME_PATTERN = "^[0-9a-f]{32}$";
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
    private final MC modelConverter;
    private final IC itemConverter;
    private final VIC versionedItemConverter;
    private final VaultService vaultService;
    private final Function<VaultFake, S> toEntityVault;

    protected BaseController(@NonNull final MC modelConverter, @NonNull final IC itemConverter, @NonNull final VIC versionedItemConverter,
                             @NonNull final VaultService vaultService, final Function<VaultFake, S> toEntityVault) {
        this.modelConverter = modelConverter;
        this.itemConverter = itemConverter;
        this.versionedItemConverter = versionedItemConverter;
        this.vaultService = vaultService;
        this.toEntityVault = toEntityVault;
    }

    protected M getModelById(final S entityVaultFake, final V entityId) {
        final E entity = entityVaultFake.getEntities().getReadOnlyEntity(entityId);
        return modelConverter.convert(entity);
    }

    protected DM getDeletedModelById(final S entityVaultFake, final V entityId) {
        final E entity = entityVaultFake.getDeletedEntities().getReadOnlyEntity(entityId);
        return modelConverter.convertDeleted(entity);
    }

    protected M convertDetails(final E entity) {
        return modelConverter.convert(entity);
    }

    protected KeyVaultItemListModel<I> getPageOfItemVersions(
            final URI baseUri, final String name, final int limit, final int offset, final String uriPath) {
        final S entityVaultFake = getVaultByUri(baseUri);
        final K entityId = entityId(baseUri, name);
        final Deque<String> allItems = entityVaultFake.getEntities().getVersions(entityId);
        final List<I> items = filterList(limit, offset, allItems, v -> {
            final E entity = getEntityByNameAndVersion(baseUri, name, v);
            return versionedItemConverter.convert(entity);
        });
        final URI nextUri = getNextUri(baseUri + uriPath, allItems, items, limit, offset);
        return listModel(items, nextUri);
    }

    @SuppressWarnings("SameParameterValue")
    protected KeyVaultItemListModel<I> getPageOfItems(final URI baseUri, final int limit, final int offset, final String uriPath) {
        final S entityVaultFake = getVaultByUri(baseUri);
        final List<E> allItems = entityVaultFake.getEntities().listLatestEntities();
        final List<I> items = filterList(limit, offset, allItems, itemConverter::convert);
        final URI nextUri = getNextUri(baseUri + uriPath, allItems, items, limit, offset);
        return listModel(items, nextUri);
    }

    @SuppressWarnings("SameParameterValue")
    protected KeyVaultItemListModel<I> getPageOfDeletedItems(final URI baseUri, final int limit, final int offset, final String uriPath) {
        final S entityVaultFake = getVaultByUri(baseUri);
        final List<E> allItems = entityVaultFake.getDeletedEntities().listLatestEntities();
        final List<I> items = filterList(limit, offset, allItems, itemConverter::convertDeleted);
        final URI nextUri = getNextUri(baseUri + uriPath, allItems, items, limit, offset);
        return listModel(items, nextUri);
    }

    protected E getEntityByNameAndVersion(final URI baseUri, final String name, final String version) {
        final S vaultFake = getVaultByUri(baseUri);
        final V entityId = versionedEntityId(baseUri, name, version);
        return vaultFake.getEntities().getReadOnlyEntity(entityId);
    }

    protected M getLatestEntityModel(final URI baseUri, final String name) {
        final S vaultFake = getVaultByUri(baseUri);
        final V entityId = vaultFake.getEntities().getLatestVersionOfEntity(entityId(baseUri, name));
        return getModelById(vaultFake, entityId);
    }

    protected S getVaultByUri(final URI baseUri) {
        return Optional.ofNullable(vaultService.findByUri(baseUri))
                .map(toEntityVault)
                .orElseThrow(() -> new VaultNotFoundException(baseUri));
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

    protected KeyVaultItemListModel<I> listModel(final List<I> items, final URI nextUri) {
        return new KeyVaultItemListModel<>(items, nextUri);
    }

    protected abstract V versionedEntityId(URI baseUri, String name, String version);

    protected abstract K entityId(URI baseUri, String name);

    private URI getNextUri(final String prefix, final Collection<?> allItems,
                           final Collection<?> items, final int limit, final int offset) {
        URI nextUri = null;
        if (hasMorePages(limit, offset, allItems)) {
            nextUri = URI.create(prefix + pageSuffix(limit, offset + items.size()));
        }
        return nextUri;
    }

    private <FR> List<I> filterList(
            final int limit, final int offset, final Collection<FR> allItems, final Function<FR, I> mapper) {
        return allItems.stream()
                .skip(offset)
                .limit(limit)
                .map(mapper)
                .collect(Collectors.toList());
    }

    private boolean hasMorePages(final int limit, final int offset, final Collection<?> allItems) {
        return limit + offset < allItems.size();
    }

    private String pageSuffix(final int maxResults, final int skip) {
        return "?" + API_VERSION_7_2 + "&" + SKIP_TOKEN_PARAM + "=" + skip + "&" + MAX_RESULTS_PARAM + "=" + maxResults;
    }

}
