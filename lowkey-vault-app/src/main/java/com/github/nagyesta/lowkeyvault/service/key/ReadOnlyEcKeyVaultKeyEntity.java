package com.github.nagyesta.lowkeyvault.service.key;

import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyCurveName;

public interface ReadOnlyEcKeyVaultKeyEntity extends ReadOnlyKeyVaultKeyEntity {

    byte[] getX();

    byte[] getY();

    KeyCurveName getKeyCurveName();
}
