package com.github.nagyesta.lowkeyvault.service.vault.impl;

import com.github.nagyesta.lowkeyvault.model.v7_2.common.constants.RecoveryLevel;
import com.github.nagyesta.lowkeyvault.service.exception.AlreadyExistsException;
import com.github.nagyesta.lowkeyvault.service.exception.NotFoundException;
import com.github.nagyesta.lowkeyvault.service.vault.VaultFake;
import com.github.nagyesta.lowkeyvault.service.vault.VaultService;
import org.springframework.util.Assert;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

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
    public VaultFake create(final URI uri, final RecoveryLevel recoveryLevel, final Integer recoverableDays) {
        return create(uri, () -> new VaultFakeImpl(uri, recoveryLevel, recoverableDays));
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
            return vaultFakes.remove(found);
        }
    }

    @Override
    public void timeShift(final int offsetSeconds)  {
        Assert.isTrue(offsetSeconds > 0, "Offset must be positive.");
        vaultFakes.forEach(vaultFake -> vaultFake.timeShift(offsetSeconds));
        purgeExpired();
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
