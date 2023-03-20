package com.github.nagyesta.lowkeyvault.context;

import com.github.nagyesta.lowkeyvault.model.common.ApiConstants;

import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public interface ApiVersionAware {

    /**
     * Supported API version collection containing only 7.2.
     */
    SortedSet<String> V7_2 = new TreeSet<>(Set.of(ApiConstants.V_7_2));
    /**
     * Supported API version collection containing only 7.3.
     */
    SortedSet<String> V7_3 = new TreeSet<>(Set.of(ApiConstants.V_7_3));
    /**
     * Supported API version collection containing both 7.2 and 7.3.
     */
    SortedSet<String> V7_2_AND_V7_3 = new TreeSet<>(Set.of(ApiConstants.V_7_2, ApiConstants.V_7_3));

    SortedSet<String> supportedVersions();
}
