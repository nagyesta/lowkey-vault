package com.github.nagyesta.lowkeyvault.service.key.impl;

import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyType;
import lombok.Data;
import lombok.NonNull;

@Data
public class KeyCreationInput<T> {

    @NonNull
    private KeyType keyType;

    private T keyParameter;

    public KeyCreationInput(@NonNull final KeyType keyType, final T keyParameter) {
        this.keyType = keyType;
        this.keyParameter = keyParameter;
    }
}
