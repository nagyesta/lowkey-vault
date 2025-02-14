package com.github.nagyesta.lowkeyvault.model.json.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.zip.GZIPOutputStream;

/**
 * Serializer zipping json snippets and encoding with base64.
 *
 * @param <E> The type of the entity.
 */
@Slf4j
public abstract class AbstractBase64ZipSerializer<E> extends JsonSerializer<E> {

    private final Base64Serializer base64Serializer;
    private final ObjectMapper objectMapper;

    protected AbstractBase64ZipSerializer(final Base64Serializer base64Serializer, final ObjectMapper objectMapper) {
        this.base64Serializer = base64Serializer;
        this.objectMapper = objectMapper;
    }

    @Override
    public void serialize(final E value, final JsonGenerator gen,
                          final SerializerProvider serializers) throws IOException {
        final String base64 = Optional.ofNullable(value)
                .map(this::compressObject)
                .orElse(null);
        if (base64 != null) {
            gen.writeString(base64);
        } else {
            gen.writeNull();
        }
    }

    private String compressObject(final E value) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream)) {
            final String json = objectMapper.writer().writeValueAsString(value);
            gzipOutputStream.write(json.getBytes(StandardCharsets.UTF_8));
            gzipOutputStream.flush();
            gzipOutputStream.finish();
            final byte[] byteArray = byteArrayOutputStream.toByteArray();
            return base64Serializer.base64Encode(byteArray);
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
            throw new IllegalArgumentException("Unable to compress input.");
        }
    }
}
