package com.github.nagyesta.lowkeyvault.model.common;

public final class ApiConstants {

    /**
     * Parameter for getting the request base URI.
     */
    public static final String REQUEST_BASE_URI = "requestBaseUri";
    /**
     * The version of the v7.2 API.
     */
    public static final String V_7_2 = "7.2";

    private ApiConstants() {
        throw new IllegalCallerException("Utility cannot be instantiated.");
    }
}
