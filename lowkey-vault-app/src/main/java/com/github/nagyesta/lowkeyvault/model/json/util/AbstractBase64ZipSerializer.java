package com.github.nagyesta.lowkeyvault.model.json.util;

import lombok.extern.slf4j.Slf4j;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.zip.GZIPOutputStream;

/**
 * Serializer zipping JSON snippets and encoding with base64.
 *
 * @param <E> The type of the entity.
 */
@Slf4j
public abstract class AbstractBase64ZipSerializer<E> extends ValueSerializer<E> {

    private final Base64Serializer base64Serializer;
    private final ObjectMapper objectMapper;

    protected AbstractBase64ZipSerializer(
            final Base64Serializer base64Serializer,
            final ObjectMapper objectMapper) {
        this.base64Serializer = base64Serializer;
        this.objectMapper = objectMapper;
    }

    @Override
    public void serialize(
            final E value,
            final JsonGenerator gen,
            final SerializationContext serializers) {
        final var base64 = Optional.ofNullable(value)
                .map(this::compressObject)
                .orElse(null);
        if (base64 != null) {
            gen.writeString(base64);
        } else {
            gen.writeNull();
        }
    }

    private String compressObject(final E value) {
        try (var byteArrayOutputStream = new ByteArrayOutputStream();
             var gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream)) {
            final var json = objectMapper.writer().writeValueAsString(value);
            gzipOutputStream.write(json.getBytes(StandardCharsets.UTF_8));
            gzipOutputStream.flush();
            gzipOutputStream.finish();
            final var byteArray = byteArrayOutputStream.toByteArray();
            return base64Serializer.base64Encode(byteArray);
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
            throw new IllegalArgumentException("Unable to compress input.");
        }
    }
}
