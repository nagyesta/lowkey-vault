package com.github.nagyesta.lowkeyvault.model.v7_3.key.constants;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;

public enum LifetimeActionType {
    /**
     * Notification triggers.
     */
    NOTIFY("notify"),
    /**
     * Automatic rotation trigger.
     */
    ROTATE("rotate");

    private final String value;

    LifetimeActionType(final String value) {
        this.value = value;
    }

    @JsonCreator
    public static @Nullable LifetimeActionType forValue(@Nullable final String name) {
        return Arrays.stream(values())
                .filter(actionType -> actionType.getValue().equals(name))
                .findFirst()
                .orElse(null);
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
