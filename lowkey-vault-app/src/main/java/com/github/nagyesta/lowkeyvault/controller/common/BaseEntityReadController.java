package com.github.nagyesta.lowkeyvault.controller.common;

import com.github.nagyesta.lowkeyvault.controller.ErrorHandlingAwareController;
import com.github.nagyesta.lowkeyvault.service.EntityId;
import com.github.nagyesta.lowkeyvault.service.common.BaseVaultEntity;
import com.github.nagyesta.lowkeyvault.service.common.BaseVaultFake;
import com.github.nagyesta.lowkeyvault.service.exception.NotFoundException;
import com.github.nagyesta.lowkeyvault.service.vault.VaultFake;
import com.github.nagyesta.lowkeyvault.service.vault.VaultService;
import lombok.NonNull;

import java.net.URI;
import java.util.Optional;
import java.util.function.Function;

/**
 * The base implementation of the backup/restore controllers.
 *
 * @param <K>   The type of the entity id (not versioned).
 * @param <V>   The versioned entity id type.
 * @param <E>   The entity type.
 * @param <S>   The fake type holding the entities.
 */
public abstract class BaseEntityReadController<K extends EntityId, V extends K, E extends BaseVaultEntity<V>,
        S extends BaseVaultFake<K, V, E>> extends ErrorHandlingAwareController {

    /**
     * RegExp of entity names (key name, secret name, certificate name).
     */
    protected static final String NAME_PATTERN = "^[0-9a-zA-Z-]+$";
    /**
     * RegExp of entity version identifiers (key version, secret version, certificate version).
     */
    protected static final String VERSION_NAME_PATTERN = "^[0-9a-f]{32}$";
    private final VaultService vaultService;
    private final Function<VaultFake, S> toEntityVault;

    protected BaseEntityReadController(@NonNull final VaultService vaultService,
                                       @org.springframework.lang.NonNull final Function<VaultFake, S> toEntityVault) {
        this.vaultService = vaultService;
        this.toEntityVault = toEntityVault;
    }

    protected E getEntityByNameAndVersion(final URI baseUri, final String name, final String version) {
        final var vaultFake = getVaultByUri(baseUri);
        final var entityId = versionedEntityId(baseUri, name, version);
        return vaultFake.getEntities().getReadOnlyEntity(entityId);
    }

    protected S getVaultByUri(final URI baseUri) {
        return Optional.of(vaultService.findByUri(baseUri))
                .map(toEntityVault)
                .orElseThrow(() -> new NotFoundException("Vault not found by base URI: " + baseUri));
    }

    protected abstract V versionedEntityId(URI baseUri, String name, String version);

    protected abstract K entityId(URI baseUri, String name);

    protected abstract String apiVersion();

}
