package com.github.nagyesta.lowkeyvault.model.json.util;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

import java.util.Base64;
import java.util.Optional;

public class Base64Serializer extends ValueSerializer<byte[]> {

    private static final Base64.Encoder DEFAULT_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private final Base64.Encoder encoder;

    public Base64Serializer() {
        this(DEFAULT_ENCODER);
    }

    protected Base64Serializer(final Base64.Encoder encoder) {
        this.encoder = encoder;
    }

    @Override
    public void serialize(
            final byte[] value,
            final JsonGenerator generator,
            final SerializationContext provider) {
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
