package com.github.nagyesta.lowkeyvault.service.key.impl;

import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyCurveName;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

@Getter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class EcKeyCreationInput extends KeyCreationInput<KeyCurveName> {

    public EcKeyCreationInput(@NonNull final KeyType keyType,
                              final KeyCurveName keyParameter) {
        super(keyType, keyParameter);
        Assert.isTrue(keyType.isEc(), "KeyType must be EC.");
        Assert.notNull(keyParameter, "KeyCurveName must not be null.");
    }
}
