package com.github.nagyesta.lowkeyvault.openapi;

public final class Examples {
    /**
     * Example base URI of a vault.
     */
    public static final String BASE_URI = "https://vault.localhost:8443";
    /**
     * The literal 1 as a String.
     */
    public static final String ONE = "1";
    /**
     * The literal 42 as a String.
     */
    public static final String FORTY_TWO = "42";
    /**
     * The value of {@link com.github.nagyesta.lowkeyvault.model.v7_2.common.constants.RecoveryLevel#CUSTOMIZED_RECOVERABLE_AND_PURGEABLE}.
     */
    public static final String CUSTOMIZED_RECOVERABLE_PURGEABLE = "CustomizedRecoverable+Purgeable";
    /**
     * UTC Epoch seconds format of 2022-01-02 03:04:05 AM.
     */
    public static final String EPOCH_SECONDS_2022_01_02_AM_03H_04M_05S = "1641092645";
    /**
     * Example value of an exception class.
     */
    public static final String EXCEPTION = "java.lang.IllegalArgumentException";
    /**
     * Error message example.
     */
    public static final String ERROR_MESSAGE = "BaseUri must be populated.";

    private Examples() {
        throw new IllegalCallerException("Utility cannot be instantiated.");
    }
}
