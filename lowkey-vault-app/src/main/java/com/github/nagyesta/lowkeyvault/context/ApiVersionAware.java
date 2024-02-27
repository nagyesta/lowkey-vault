package com.github.nagyesta.lowkeyvault.context;

import com.github.nagyesta.lowkeyvault.model.common.ApiConstants;

import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public interface ApiVersionAware {

    /**
     * Supported API version collection containing all 7.3+ versions.
     */
    SortedSet<String> V7_3_AND_LATER = new TreeSet<>(Set.of(ApiConstants.V_7_3, ApiConstants.V_7_4, ApiConstants.V_7_5));
    /**
     * Supported API version collection containing all versions (7.2, 7.3, 7.4 and 7.5).
     */
    SortedSet<String> ALL_VERSIONS = new TreeSet<>(Set.of(ApiConstants.V_7_2, ApiConstants.V_7_3, ApiConstants.V_7_4, ApiConstants.V_7_5));

    SortedSet<String> supportedVersions();
}
