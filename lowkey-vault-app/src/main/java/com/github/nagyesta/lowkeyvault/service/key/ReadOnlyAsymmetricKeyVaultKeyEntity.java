package com.github.nagyesta.lowkeyvault.service.key;

import java.security.KeyPair;

public interface ReadOnlyAsymmetricKeyVaultKeyEntity extends ReadOnlyKeyVaultKeyEntity {

    KeyPair getKey();
}
