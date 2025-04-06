package com.github.nagyesta.lowkeyvault.service.key.impl;

import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

import java.math.BigInteger;

@Getter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class RsaKeyCreationInput
        extends KeyCreationInput<Integer> {

    private final BigInteger publicExponent;

    public RsaKeyCreationInput(
            @NonNull final KeyType keyType,
            final Integer keyParameter,
            final BigInteger publicExponent) {
        super(keyType, keyParameter);
        Assert.isTrue(keyType.isRsa(), "KeyType must be RSA.");
        this.publicExponent = publicExponent;
    }
}
