package com.github.nagyesta.lowkeyvault.model.v7_2.key.constants;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

/**
 * Defines values for KeyOperation.
 */
public enum KeyOperation {

    /**
     * Static value Encrypt for KeyOperation.
     */
    ENCRYPT("encrypt"),
    /**
     * Static value Decrypt for KeyOperation.
     */
    DECRYPT("decrypt"),
    /**
     * Static value Sign for KeyOperation.
     */
    SIGN("sign"),
    /**
     * Static value Verify for KeyOperation.
     */
    VERIFY("verify"),
    /**
     * Static value Wrap Key for KeyOperation.
     */
    WRAP_KEY("wrapKey"),
    /**
     * Static value Unwrap Key for KeyOperation.
     */
    UNWRAP_KEY("unwrapKey"),
    /**
     * Static value Import for KeyOperation.
     */
    IMPORT("import");

    private final String value;

    KeyOperation(final String value) {
        this.value = value;
    }

    @JsonCreator
    public static KeyOperation forValue(final String name) {
        return Arrays.stream(values())
                .filter(keyType -> keyType.getValue().equals(name))
                .findFirst()
                .orElse(null);
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
