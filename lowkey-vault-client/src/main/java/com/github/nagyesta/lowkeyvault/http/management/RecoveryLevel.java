package com.github.nagyesta.lowkeyvault.http.management;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

@SuppressWarnings("checkstyle:LineLength")
public enum RecoveryLevel {

    /**
     * Purgeable and not recoverable, subscription not protected.
     * See: <a href="https://learn.microsoft.com/en-us/rest/api/keyvault/keys/create-key/create-key?view=rest-keyvault-keys-7.4#deletionrecoverylevel">
     * Deletion Recovery Key Level</a>
     */
    PURGEABLE("Purgeable"),
    /**
     * Purgeable and recoverable, subscription not protected.
     * See: <a href="https://learn.microsoft.com/en-us/rest/api/keyvault/keys/create-key/create-key?view=rest-keyvault-keys-7.4#deletionrecoverylevel">
     * Deletion Recovery Key Level</a>
     */
    RECOVERABLE_AND_PURGEABLE("Recoverable+Purgeable"),
    /**
     * Recoverable, not purgeable, subscription not protected.
     * See: <a href="https://learn.microsoft.com/en-us/rest/api/keyvault/keys/create-key/create-key?view=rest-keyvault-keys-7.4#deletionrecoverylevel">
     * Deletion Recovery Key Level</a>
     */
    RECOVERABLE("Recoverable"),
    /**
     * Recoverable, not purgeable, subscription protected.
     * See: <a href="https://learn.microsoft.com/en-us/rest/api/keyvault/keys/create-key/create-key?view=rest-keyvault-keys-7.4#deletionrecoverylevel">
     * Deletion Recovery Key Level</a>
     */
    RECOVERABLE_AND_PROTECTED_SUBSCRIPTION("Recoverable+ProtectedSubscription"),
    /**
     * Recoverable for a customized time, purgeable, subscription not protected.
     * See: <a href="https://learn.microsoft.com/en-us/rest/api/keyvault/keys/create-key/create-key?view=rest-keyvault-keys-7.4#deletionrecoverylevel">
     * Deletion Recovery Key Level</a>
     */
    CUSTOMIZED_RECOVERABLE_AND_PURGEABLE("CustomizedRecoverable+Purgeable"),
    /**
     * Recoverable for a customized time, not purgeable, subscription not protected.
     * See: <a href="https://learn.microsoft.com/en-us/rest/api/keyvault/keys/create-key/create-key?view=rest-keyvault-keys-7.4#deletionrecoverylevel">
     * Deletion Recovery Key Level</a>
     */
    CUSTOMIZED_RECOVERABLE("CustomizedRecoverable"),
    /**
     * Recoverable for a customized time, not purgeable, subscription protected.
     * See: <a href="https://learn.microsoft.com/en-us/rest/api/keyvault/keys/create-key/create-key?view=rest-keyvault-keys-7.4#deletionrecoverylevel">
     * Deletion Recovery Key Level</a>
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
