package com.github.nagyesta.lowkeyvault.model.json.util;

import lombok.extern.slf4j.Slf4j;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.ValueDeserializer;

import java.io.ByteArrayInputStream;
import java.util.Optional;
import java.util.zip.GZIPInputStream;

/**
 * Deserializer Base64 decoding and unzipping json snippets.
 *
 * @param <E> The type of the entity.
 */
@Slf4j
public abstract class AbstractBase64ZipDeserializer<E>
        extends ValueDeserializer<E> {

    private final Base64Deserializer base64Deserializer;
    private final ObjectMapper objectMapper;

    protected AbstractBase64ZipDeserializer(
            final Base64Deserializer base64Deserializer,
            final ObjectMapper objectMapper) {
        this.base64Deserializer = base64Deserializer;
        this.objectMapper = objectMapper;
    }

    @Override
    public E deserialize(
            final JsonParser jsonParser,
            final DeserializationContext context) {
        final var bytes = Optional.ofNullable(base64Deserializer.deserializeBase64(jsonParser));
        return bytes.filter(v -> v.length > 0)
                .map(this::decompressWrappedObject)
                .orElse(null);
    }

    private E decompressWrappedObject(final byte[] bytes) {
        try (var byteArrayInputStream = new ByteArrayInputStream(bytes);
             var gzipInputStream = new GZIPInputStream(byteArrayInputStream)) {
            final var json = new String(gzipInputStream.readAllBytes());
            return objectMapper.readerFor(getType()).readValue(json);
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
            throw new IllegalArgumentException("Unable to decompress input.");
        }
    }

    protected abstract Class<E> getType();
}
