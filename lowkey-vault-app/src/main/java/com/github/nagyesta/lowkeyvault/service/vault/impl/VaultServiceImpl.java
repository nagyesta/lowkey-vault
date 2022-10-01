package com.github.nagyesta.lowkeyvault.service.vault.impl;

import com.github.nagyesta.lowkeyvault.model.v7_2.common.constants.RecoveryLevel;
import com.github.nagyesta.lowkeyvault.service.exception.AlreadyExistsException;
import com.github.nagyesta.lowkeyvault.service.exception.NotFoundException;
import com.github.nagyesta.lowkeyvault.service.vault.VaultFake;
import com.github.nagyesta.lowkeyvault.service.vault.VaultService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import java.net.URI;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Slf4j
public class VaultServiceImpl implements VaultService {

    private final Set<VaultFake> vaultFakes = new CopyOnWriteArraySet<>();

    @Override
    public VaultFake findByUri(final URI uri) {
        return findByUriAndDeleteStatus(uri, VaultFake::isActive)
                .orElseThrow(() -> new NotFoundException("Unable to find active vault: " + uri));
    }

    @Override
    public VaultFake findByUriIncludeDeleted(final URI uri) {
        return findByUriAndDeleteStatus(uri, v -> true)
                .orElseThrow(() -> new NotFoundException("Unable to find vault: " + uri));
    }

    @Override
    public VaultFake create(final URI uri) {
        return create(uri, () -> new VaultFakeImpl(uri));
    }

    @Override
    public VaultFake create(final URI uri, final RecoveryLevel recoveryLevel, final Integer recoverableDays, final Set<URI> aliases) {
        final Optional<Set<URI>> optionalAliases = Optional.ofNullable(aliases);
        optionalAliases.stream().flatMap(Set::stream).forEach(alias -> {
            Assert.isTrue(!uri.equals(alias), "Base URI cannot match alias: " + alias);
            if (findByUriAndDeleteStatus(alias, v -> true).isPresent()) {
                throw new AlreadyExistsException("Vault alias already exists: " + alias);
            }
        });
        final VaultFake vaultFake = create(uri, () -> new VaultFakeImpl(uri, recoveryLevel, recoverableDays));
        optionalAliases.ifPresent(vaultFake::setAliases);
        return vaultFake;
    }

    @Override
    public List<VaultFake> list() {
        return vaultFakes.stream()
                .filter(VaultFake::isActive)
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public List<VaultFake> listDeleted() {
        purgeExpired();
        return vaultFakes.stream()
                .filter(VaultFake::isDeleted)
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public boolean delete(final URI uri) {
        synchronized (vaultFakes) {
            final Optional<VaultFake> vaultFake = findByUriAndDeleteStatus(uri, VaultFake::isActive);
            boolean deleted = false;
            if (vaultFake.isPresent()) {
                final VaultFake found = vaultFake.get();
                log.info("Deleting vault with URI: {}", uri);
                found.delete();
                deleted = true;
            }
            return deleted;
        }
    }

    @Override
    public void recover(final URI uri) {
        purgeExpired();
        synchronized (vaultFakes) {
            final Optional<VaultFake> vaultFake = findByUriAndDeleteStatus(uri, VaultFake::isDeleted);
            final VaultFake found = vaultFake
                    .orElseThrow(() -> new NotFoundException("Unable to find deleted vault: " + uri));
            log.info("Recovering vault with URI: {}", uri);
            found.recover();
        }
    }

    @Override
    public boolean purge(final URI uri) {
        purgeExpired();
        synchronized (vaultFakes) {
            final Optional<VaultFake> vaultFake = findByUriAndDeleteStatus(uri, VaultFake::isDeleted);
            final VaultFake found = vaultFake
                    .orElseThrow(() -> new NotFoundException("Unable to find deleted vault: " + uri));
            log.info("Purging vault with URI: {}", uri);
            return vaultFakes.remove(found);
        }
    }

    @Override
    public void timeShift(final int offsetSeconds) {
        Assert.isTrue(offsetSeconds > 0, "Offset must be positive.");
        log.info("Performing time shift with {} seconds for all vaults.", offsetSeconds);
        vaultFakes.forEach(vaultFake -> vaultFake.timeShift(offsetSeconds));
        purgeExpired();
    }

    @Override
    public VaultFake updateAlias(final URI baseUri, final URI add, final URI remove) {
        log.info("Updating aliases of: {} , adding: {}, removing: {}", baseUri, add, remove);
        Assert.isTrue(add != null || remove != null, "At least one of the add/remove parameters needs to be populated.");
        Assert.isTrue(!Objects.equals(add, remove), "The URL we want to add and remove, must be different.");
        final VaultFake fake = findByUriIncludeDeleted(baseUri);

        final TreeSet<URI> aliases = new TreeSet<>(fake.aliases());
        Optional.ofNullable(add).ifPresent(alias -> {
            if (findByUriAndDeleteStatus(add, v -> true).isPresent()) {
                throw new AlreadyExistsException("Vault alias already exists: " + add);
            }
            aliases.add(alias);
        });
        Optional.ofNullable(remove).ifPresent(aliases::remove);
        fake.setAliases(aliases);
        return fake;
    }

    private Optional<VaultFake> findByUriAndDeleteStatus(final URI uri, final Predicate<VaultFake> deletedPredicate) {
        return vaultFakes.stream()
                .filter(v -> v.matches(uri))
                .filter(deletedPredicate)
                .findFirst();
    }

    private VaultFake create(final URI uri, final Supplier<VaultFake> vaultFakeSupplier) {
        synchronized (vaultFakes) {
            if (exists(uri)) {
                throw new AlreadyExistsException("Vault is already created with uri: " + uri);
            }
            log.info("Creating vault for URI: {}", uri);
            final VaultFake vaultFake = vaultFakeSupplier.get();
            vaultFakes.add(vaultFake);
            return vaultFake;
        }
    }

    private boolean exists(final URI uri) {
        return vaultFakes.stream()
                .anyMatch(v -> v.matches(uri));
    }

    private void purgeExpired() {
        synchronized (vaultFakes) {
            vaultFakes.removeIf(VaultFake::isExpired);
        }
    }
}
