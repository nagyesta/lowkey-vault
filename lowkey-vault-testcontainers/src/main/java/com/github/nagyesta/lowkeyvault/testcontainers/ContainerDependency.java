package com.github.nagyesta.lowkeyvault.testcontainers;

import org.testcontainers.lifecycle.Startable;

import java.util.Collections;
import java.util.Map;
import java.util.function.Function;

public record ContainerDependency<T extends Startable>(
        T container,
        Function<T, Map<String, String>> secretSupplier) {

    ContainerDependency(final T container) {
        this(container, c -> Collections.emptyMap());
    }

    public Map<String, String> getSecrets() {
        return secretSupplier.apply(container);
    }

}
