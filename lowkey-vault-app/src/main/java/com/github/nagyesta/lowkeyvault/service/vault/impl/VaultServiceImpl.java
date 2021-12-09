package com.github.nagyesta.lowkeyvault.service.vault.impl;

import com.github.nagyesta.lowkeyvault.service.vault.VaultFake;
import com.github.nagyesta.lowkeyvault.service.vault.VaultService;

import java.net.URI;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class VaultServiceImpl implements VaultService {

    private final Set<VaultFake> vaultFakes = new CopyOnWriteArraySet<>();

    @Override
    public VaultFake findByUri(final URI uri) {
        return vaultFakes.stream()
                .filter(v -> v.matches(uri))
                .findFirst()
                .orElse(null);
    }

    @Override
    public VaultFake create(final URI uri) {
        synchronized (vaultFakes) {
            Optional<VaultFake> vaultFake = Optional.ofNullable(findByUri(uri));
            if (vaultFake.isEmpty()) {
                vaultFake = Optional.of(new VaultFakeImpl(uri));
                vaultFakes.add(vaultFake.get());
            }
            return vaultFake.get();
        }
    }

}
