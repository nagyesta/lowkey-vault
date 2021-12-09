package com.github.nagyesta.lowkeyvault.service.vault;

import java.net.URI;

public interface VaultService {

    VaultFake findByUri(URI uri);

    VaultFake create(URI uri);
}
