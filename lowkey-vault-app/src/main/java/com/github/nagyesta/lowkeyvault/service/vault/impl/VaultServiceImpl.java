package com.github.nagyesta.lowkeyvault.service.vault.impl;

import com.github.nagyesta.lowkeyvault.service.vault.VaultService;
import com.github.nagyesta.lowkeyvault.service.vault.VaultStub;

import java.net.URI;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class VaultServiceImpl implements VaultService {

    private final Set<VaultStub> vaultStubs = new CopyOnWriteArraySet<>();

    @Override
    public VaultStub findByUri(final URI uri) {
        return vaultStubs.stream()
                .filter(v -> v.matches(uri))
                .findFirst()
                .orElse(null);
    }

    @Override
    public VaultStub create(final URI uri) {
        synchronized (vaultStubs) {
            Optional<VaultStub> vaultStub = Optional.ofNullable(findByUri(uri));
            if (vaultStub.isEmpty()) {
                vaultStub = Optional.of(new VaultStubImpl(uri));
                vaultStubs.add(vaultStub.get());
            }
            return vaultStub.get();
        }
    }

}
