package com.github.nagyesta.lowkeyvault.model.v7_2.common.constants;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;

public enum RecoveryLevel {

    /**
     * Purgeable and not recoverable, subscription not protected.
     * See: <a href="https://docs.microsoft.com/en-us/rest/api/keyvault/create-key/create-key#deletionrecoverylevel">source</a>
     */
    PURGEABLE("Purgeable") {
        @Override
        public boolean isPurgeable() {
            return true;
        }

        @Override
        public boolean isRecoverable() {
            return false;
        }

        @Override
        public void checkValidRecoverableDays(@Nullable final Integer recoverableDays) {
            if (recoverableDays != null) {
                throw new IllegalArgumentException("Recoverable days must be null for PURGEABLE.");
            }
        }
    },
    /**
     * Purgeable and recoverable, subscription not protected.
     * See: <a href="https://docs.microsoft.com/en-us/rest/api/keyvault/create-key/create-key#deletionrecoverylevel">source</a>
     */
    RECOVERABLE_AND_PURGEABLE("Recoverable+Purgeable") {
        @Override
        public boolean isPurgeable() {
            return true;
        }
    },
    /**
     * Recoverable, not purgeable, subscription not protected.
     * See: <a href="https://docs.microsoft.com/en-us/rest/api/keyvault/create-key/create-key#deletionrecoverylevel">source</a>
     */
    RECOVERABLE("Recoverable"),
    /**
     * Recoverable, not purgeable, subscription protected.
     * See: <a href="https://docs.microsoft.com/en-us/rest/api/keyvault/create-key/create-key#deletionrecoverylevel">source</a>
     */
    RECOVERABLE_AND_PROTECTED_SUBSCRIPTION("Recoverable+ProtectedSubscription") {
        @Override
        public boolean isSubscriptionProtected() {
            return true;
        }
    },
    /**
     * Recoverable for a customized time, purgeable, subscription not protected.
     * See: <a href="https://docs.microsoft.com/en-us/rest/api/keyvault/create-key/create-key#deletionrecoverylevel">source</a>
     */
    CUSTOMIZED_RECOVERABLE_AND_PURGEABLE("CustomizedRecoverable+Purgeable") {
        @Override
        public boolean isPurgeable() {
            return true;
        }

        @Override
        public boolean isCustomized() {
            return true;
        }
    },
    /**
     * Recoverable for a customized time, not purgeable, subscription not protected.
     * See: <a href="https://docs.microsoft.com/en-us/rest/api/keyvault/create-key/create-key#deletionrecoverylevel">source</a>
     */
    CUSTOMIZED_RECOVERABLE("CustomizedRecoverable") {
        @Override
        public boolean isCustomized() {
            return true;
        }
    },

    /**
     * Recoverable for a customized time, not purgeable, subscription protected.
     * See: <a href="https://docs.microsoft.com/en-us/rest/api/keyvault/create-key/create-key#deletionrecoverylevel">source</a>
     */
    CUSTOMIZED_RECOVERABLE_AND_PROTECTED_SUBSCRIPTION("CustomizedRecoverable+ProtectedSubscription") {
        @Override
        public boolean isSubscriptionProtected() {
            return true;
        }

        @Override
        public boolean isCustomized() {
            return true;
        }
    };

    /**
     * The minimum number of days entities are recoverable for.
     */
    public static final int MIN_RECOVERABLE_DAYS_INCLUSIVE = 7;
    /**
     * The maximum number of days entities are recoverable for.
     */
    public static final int MAX_RECOVERABLE_DAYS_INCLUSIVE = 90;
    private final String value;

    RecoveryLevel(final String value) {
        this.value = value;
    }

    @JsonCreator
    public static RecoveryLevel forValue(@Nullable final String value) {
        return Arrays.stream(RecoveryLevel.values())
                .filter(r -> r.getValue().equals(value))
                .findFirst()
                .orElse(PURGEABLE);
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public boolean isRecoverable() {
        return true;
    }

    public boolean isPurgeable() {
        return false;
    }

    public boolean isSubscriptionProtected() {
        return false;
    }

    public boolean isCustomized() {
        return false;
    }

    public void checkValidRecoverableDays(@Nullable final Integer recoverableDays) {
        if (recoverableDays == null) {
            throw new IllegalArgumentException("Recoverable days must not be null.");
        } else if (isCustomized()) {
            validateCustomizedRecoverableDays(recoverableDays);
        } else if (recoverableDays != MAX_RECOVERABLE_DAYS_INCLUSIVE) {
            throw new IllegalArgumentException("Recoverable days must be 90 if not customised.");
        }
    }

    private void validateCustomizedRecoverableDays(final Integer recoverableDays) {
        if (recoverableDays < MIN_RECOVERABLE_DAYS_INCLUSIVE || recoverableDays > MAX_RECOVERABLE_DAYS_INCLUSIVE) {
            throw new IllegalArgumentException("Recoverable days must be at least 7 and maximum 90.");
        }
    }
}
