package com.github.nagyesta.lowkeyvault.service.key.impl;

import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

@Getter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OctKeyCreationInput extends KeyCreationInput<Integer> {

    public OctKeyCreationInput(@NonNull final KeyType keyType,
                               final Integer keyParameter) {
        super(keyType, keyParameter);
        Assert.isTrue(keyType.isOct(), "KeyType must be OCT.");
    }
}
