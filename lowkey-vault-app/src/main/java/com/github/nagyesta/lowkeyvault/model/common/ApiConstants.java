package com.github.nagyesta.lowkeyvault.model.common;

import java.util.Set;

public final class ApiConstants {

    /**
     * Parameter for getting the request base URI.
     */
    public static final String REQUEST_BASE_URI = "requestBaseUri";
    /**
     * The paths which are not used by any key vault functionality.
     */
    public static final Set<String> NON_VAULT_URIS = Set.of("/ping", "/ping/", "/management/**", "/api/**", "/metadata/**");
    /**
     * The version of the v7.2 API.
     */
    public static final String V_7_2 = "7.2";
    /**
     * The version of the v7.2 API or later.
     */
    public static final String V_7_2_AND_LATER = "7.2+";
    /**
     * The version of the v7.3 API.
     */
    public static final String V_7_3 = "7.3";
    /**
     * The version of the v7.3 API or later.
     */
    public static final String V_7_3_AND_LATER = "7.3+";
    /**
     * API version parameter name.
     */
    public static final String API_VERSION_NAME = "api-version";
    /**
     * API version prefix.
     */
    public static final String API_VERSION_PREFIX = "api-version=";

    private ApiConstants() {
        throw new IllegalCallerException("Utility cannot be instantiated.");
    }
}
