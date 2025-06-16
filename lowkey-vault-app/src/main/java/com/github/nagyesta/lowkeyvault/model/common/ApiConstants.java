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
    /**
     * The version of the v7.3 API.
     */
    public static final String V_7_3 = "7.3";
    /**
     * The version of the v7.4 API.
     */
    public static final String V_7_4 = "7.4";
    /**
     * The version of the v7.5 API.
     */
    public static final String V_7_5 = "7.5";
    /**
     * The version of the v7.6 API.
     */
    public static final String V_7_6 = "7.6";
    /**
     * API version prefix.
     */
    public static final String API_VERSION_PREFIX = "api-version=";
    /**
     * API version param for 7.2.
     */
    public static final String API_VERSION_7_2 = API_VERSION_PREFIX + V_7_2;
    /**
     * API version param for 7.3.
     */
    public static final String API_VERSION_7_3 = API_VERSION_PREFIX + V_7_3;
    /**
     * API version param for 7.4.
     */
    public static final String API_VERSION_7_4 = API_VERSION_PREFIX + V_7_4;
    /**
     * API version param for 7.5.
     */
    public static final String API_VERSION_7_5 = API_VERSION_PREFIX + V_7_5;
    /**
     * API version param for 7.6.
     */
    public static final String API_VERSION_7_6 = API_VERSION_PREFIX + V_7_6;

    private ApiConstants() {
        throw new IllegalCallerException("Utility cannot be instantiated.");
    }
}
