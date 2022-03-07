package com.github.nagyesta.lowkeyvault;

import org.junit.jupiter.api.Assertions;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public final class ResourceUtils {

    private ResourceUtils() {
        throw new IllegalCallerException("Utility cannot be instantiated.");
    }

    public static String loadResourceAsString(final String resource) {
        //noinspection LocalCanBeFinal
        try (InputStream stream = ResourceUtils.class.getResourceAsStream(resource)) {
            return StreamUtils.copyToString(stream, StandardCharsets.UTF_8);
        } catch (final IOException e) {
            Assertions.fail(e.getMessage());
            return null;
        }
    }
}
