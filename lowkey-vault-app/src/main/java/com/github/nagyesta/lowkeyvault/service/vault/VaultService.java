package com.github.nagyesta.lowkeyvault.service.vault;

import java.net.URI;

public interface VaultService {

    VaultStub findByUri(URI uri);

    VaultStub create(URI uri);
}
