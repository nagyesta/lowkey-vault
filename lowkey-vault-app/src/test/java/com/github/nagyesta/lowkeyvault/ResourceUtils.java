package com.github.nagyesta.lowkeyvault;

import org.apache.tomcat.util.codec.binary.Base64;
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

    public static String loadResourceAsBase64String(final String resource) {
        //noinspection LocalCanBeFinal
        try (InputStream stream = ResourceUtils.class.getResourceAsStream(resource)) {
            final byte[] bytes = StreamUtils.copyToByteArray(stream);
            return new String(Base64.encodeBase64(bytes), StandardCharsets.UTF_8);
        } catch (final IOException e) {
            Assertions.fail(e.getMessage());
            return null;
        }
    }
}
