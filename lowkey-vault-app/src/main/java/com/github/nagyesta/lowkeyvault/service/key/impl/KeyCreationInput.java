package com.github.nagyesta.lowkeyvault.service.key.impl;

import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyType;
import lombok.Data;
import org.jspecify.annotations.Nullable;

@Data
public class KeyCreationInput<T> {

    private KeyType keyType;

    @Nullable
    private T keyParameter;

    public KeyCreationInput(
            final KeyType keyType,
            @Nullable final T keyParameter) {
        this.keyType = keyType;
        this.keyParameter = keyParameter;
    }
}
