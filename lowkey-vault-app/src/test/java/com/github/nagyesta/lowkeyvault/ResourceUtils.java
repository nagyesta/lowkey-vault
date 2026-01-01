package com.github.nagyesta.lowkeyvault;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;

public final class ResourceUtils {

    private ResourceUtils() {
        throw new IllegalCallerException("Utility cannot be instantiated.");
    }

    public static String loadResourceAsString(final String resource) {
        try (var stream = ResourceUtils.class.getResourceAsStream(resource)) {
            return StreamUtils.copyToString(stream, StandardCharsets.UTF_8);
        } catch (final IOException e) {
            Assertions.fail(e.getMessage());
            return null;
        }
    }

    public static @Nullable String loadResourceAsBase64String(final String resource) {
        final var binaryData = loadResourceAsByteArray(resource);
        return Optional.ofNullable(binaryData)
                .map(binary -> Base64.getEncoder().encodeToString(binary))
                .orElse(null);
    }

    public static byte[] loadResourceAsByteArray(final String resource) {
        try (var stream = ResourceUtils.class.getResourceAsStream(resource)) {
            return StreamUtils.copyToByteArray(stream);
        } catch (final IOException e) {
            Assertions.fail(e.getMessage());
            return null;
        }
    }
}
