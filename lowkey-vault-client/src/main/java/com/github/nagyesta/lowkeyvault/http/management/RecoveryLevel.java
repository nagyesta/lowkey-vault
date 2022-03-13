package com.github.nagyesta.lowkeyvault.http.management;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum RecoveryLevel {

    /**
     * Purgeable and not recoverable, subscription not protected.
     * See: https://docs.microsoft.com/en-us/rest/api/keyvault/create-key/create-key#deletionrecoverylevel
     */
    PURGEABLE("Purgeable"),
    /**
     * Purgeable and recoverable, subscription not protected.
     * See: https://docs.microsoft.com/en-us/rest/api/keyvault/create-key/create-key#deletionrecoverylevel
     */
    RECOVERABLE_AND_PURGEABLE("Recoverable+Purgeable"),
    /**
     * Recoverable, not purgeable, subscription not protected.
     * See: https://docs.microsoft.com/en-us/rest/api/keyvault/create-key/create-key#deletionrecoverylevel
     */
    RECOVERABLE("Recoverable"),
    /**
     * Recoverable, not purgeable, subscription protected.
     * See: https://docs.microsoft.com/en-us/rest/api/keyvault/create-key/create-key#deletionrecoverylevel
     */
    RECOVERABLE_AND_PROTECTED_SUBSCRIPTION("Recoverable+ProtectedSubscription"),
    /**
     * Recoverable for a customized time, purgeable, subscription not protected.
     * See: https://docs.microsoft.com/en-us/rest/api/keyvault/create-key/create-key#deletionrecoverylevel
     */
    CUSTOMIZED_RECOVERABLE_AND_PURGEABLE("CustomizedRecoverable+Purgeable"),
    /**
     * Recoverable for a customized time, not purgeable, subscription not protected.
     * See: https://docs.microsoft.com/en-us/rest/api/keyvault/create-key/create-key#deletionrecoverylevel
     */
    CUSTOMIZED_RECOVERABLE("CustomizedRecoverable"),
    /**
     * Recoverable for a customized time, not purgeable, subscription protected.
     * See: https://docs.microsoft.com/en-us/rest/api/keyvault/create-key/create-key#deletionrecoverylevel
     */
    CUSTOMIZED_RECOVERABLE_AND_PROTECTED_SUBSCRIPTION("CustomizedRecoverable+ProtectedSubscription");

    private final String value;

    RecoveryLevel(final String value) {
        this.value = value;
    }

    @JsonCreator
    public static RecoveryLevel forValue(final String value) {
        return Arrays.stream(RecoveryLevel.values())
                .filter(r -> r.getValue().equals(value))
                .findFirst()
                .orElse(PURGEABLE);
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
