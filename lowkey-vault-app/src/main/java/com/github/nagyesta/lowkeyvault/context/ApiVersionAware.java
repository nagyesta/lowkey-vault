package com.github.nagyesta.lowkeyvault.context;

import com.github.nagyesta.lowkeyvault.model.common.ApiConstants;

import java.util.Collections;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public interface ApiVersionAware {

    /**
     * Supported API version collection containing all 7.3+ versions.
     */
    SortedSet<String> V7_3_AND_LATER = Collections.unmodifiableSortedSet(
            new TreeSet<>(Set.of(ApiConstants.V_7_3, ApiConstants.V_7_4, ApiConstants.V_7_5, ApiConstants.V_7_6)));
    /**
     * Supported API version collection containing all versions (7.2, 7.3, 7.4, 7.5 and 7.6).
     */
    SortedSet<String> ALL_VERSIONS = Collections.unmodifiableSortedSet(
            new TreeSet<>(Set.of(ApiConstants.V_7_2, ApiConstants.V_7_3, ApiConstants.V_7_4, ApiConstants.V_7_5, ApiConstants.V_7_6)));

    SortedSet<String> supportedVersions();
}
