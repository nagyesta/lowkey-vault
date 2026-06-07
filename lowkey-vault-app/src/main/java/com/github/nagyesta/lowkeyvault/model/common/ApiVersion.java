package com.github.nagyesta.lowkeyvault.model.common;

import lombok.Getter;

import java.util.Arrays;

/**
 * Represents the API versions supported by the application.
 */
@Getter
public enum ApiVersion implements Comparable<ApiVersion> {

    /**
     * The version of the v7.2 API.
     */
    V_7_2("7.2"),
    /**
     * The version of the v7.3 API.
     */
    V_7_3("7.3"),
    /**
     * The version of the v7.4 API.
     */
    V_7_4("7.4"),
    /**
     * The version of the v7.5 API.
     */
    V_7_5("7.5"),
    /**
     * The version of the v7.6 API.
     */
    V_7_6("7.6"),
    /**
     * The version of the v2025-07-01 API.
     */
    V_2025_07_01("2025-07-01");

    private final String value;

    ApiVersion(final String value) {
        this.value = value;
    }

    public static String[] allVersionsAsString() {
        return Arrays.stream(values())
                .map(ApiVersion::getValue)
                .toArray(String[]::new);
    }

    public static ApiVersion latest() {
        return Arrays.asList(ApiVersion.values()).getLast();
    }

    public static ApiVersion parse(final String version) {
        return Arrays.stream(values())
                .filter(a -> a.getValue().equals(version))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid API version: " + version));
    }
}
