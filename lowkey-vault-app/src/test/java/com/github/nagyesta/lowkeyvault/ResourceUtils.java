package com.github.nagyesta.lowkeyvault;

import org.apache.tomcat.util.codec.binary.Base64;
import org.junit.jupiter.api.Assertions;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

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

    public static String loadResourceAsBase64String(final String resource) {
        final byte[] binaryData = loadResourceAsByteArray(resource);
        return Optional.ofNullable(binaryData)
                .map(binary -> new Base64().encodeAsString(binary))
                .orElse(null);
    }

    public static byte[] loadResourceAsByteArray(final String resource) {
        //noinspection LocalCanBeFinal
        try (InputStream stream = ResourceUtils.class.getResourceAsStream(resource)) {
            return StreamUtils.copyToByteArray(stream);
        } catch (final IOException e) {
            Assertions.fail(e.getMessage());
            return null;
        }
    }
}
