package com.github.nagyesta.lowkeyvault.model.json.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.Base64;
import java.util.Optional;

public class Base64Serializer extends JsonSerializer<byte[]> {

    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();
    private final Base64.Encoder encoder;

    public Base64Serializer() {
        this(ENCODER);
    }

    protected Base64Serializer(final Base64.Encoder encoder) {
        this.encoder = encoder;
    }

    @Override
    public void serialize(final byte[] value, final JsonGenerator generator, final SerializerProvider provider) throws IOException {
        final var text = base64Encode(value);
        if (text != null) {
            generator.writeString(text);
        } else {
            generator.writeNull();
        }
    }

    protected String base64Encode(final byte[] value) {
        final var optional = Optional.ofNullable(value);
        return optional.filter(v -> v.length > 0)
                .map(encoder::encodeToString)
                .orElse(null);
    }
}
