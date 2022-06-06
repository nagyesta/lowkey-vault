package com.github.nagyesta.lowkeyvault.model.v7_3.key;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;

import javax.validation.Valid;

@EqualsAndHashCode(callSuper = true)
public class KeyBackupList extends com.github.nagyesta.lowkeyvault.model.v7_2.key.KeyBackupList {

    @Valid
    @JsonProperty("rotationPolicy")
    private KeyRotationPolicyModel keyRotationPolicy;

    public KeyRotationPolicyModel getKeyRotationPolicy() {
        return keyRotationPolicy;
    }

    public void setKeyRotationPolicy(final KeyRotationPolicyModel keyRotationPolicy) {
        this.keyRotationPolicy = keyRotationPolicy;
    }
}
