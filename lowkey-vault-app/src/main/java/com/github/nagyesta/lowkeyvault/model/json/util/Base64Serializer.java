package com.github.nagyesta.lowkeyvault.model.json.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.Base64;
import java.util.Optional;

public class Base64Serializer extends JsonSerializer<byte[]> {

    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final String EMPTY = "";

    @Override
    public void serialize(final byte[] value, final JsonGenerator generator, final SerializerProvider provider) throws IOException {
        final Optional<byte[]> optional = Optional.ofNullable(value);
        if (optional.isPresent()) {
            final String text = optional
                    .filter(v -> v.length > 0)
                    .map(ENCODER::encodeToString)
                    .orElse(EMPTY);
            generator.writeString(text);
        } else {
            generator.writeNull();
        }
    }
}
