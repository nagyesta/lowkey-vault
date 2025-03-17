package com.github.nagyesta.lowkeyvault.testcontainers;

import org.testcontainers.lifecycle.Startable;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public final class ContainerDependency<T extends Startable> {

    private final T container;
    private final Function<T, Map<String, String>> secretSupplier;

    ContainerDependency(final T container) {
        this(container, c -> Collections.emptyMap());
    }

    ContainerDependency(
            final T container,
            final Function<T, Map<String, String>> secretSupplier) {
        this.container = container;
        this.secretSupplier = secretSupplier;
    }

    public T getContainer() {
        return container;
    }

    public Map<String, String> getSecrets() {
        return secretSupplier.apply(container);
    }

    @Override
    public boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final var that = (ContainerDependency<?>) o;
        return Objects.equals(container, that.container)
                && Objects.equals(secretSupplier, that.secretSupplier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(container, secretSupplier);
    }
}
