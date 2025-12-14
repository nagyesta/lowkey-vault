package com.github.nagyesta.lowkeyvault.model.json.util;

import org.jspecify.annotations.Nullable;
import org.springframework.util.StringUtils;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

import java.util.Base64;
import java.util.Optional;

public class Base64Deserializer extends ValueDeserializer<byte[]> {

    private static final Base64.Decoder DEFAULT_DECODER = Base64.getUrlDecoder();
    private final Base64.Decoder decoder;

    public Base64Deserializer() {
        this(DEFAULT_DECODER);
    }

    protected Base64Deserializer(final Base64.Decoder decoder) {
        this.decoder = decoder;
    }

    @Override
    public byte[] deserialize(
            final JsonParser parser,
            final DeserializationContext context) {
        final var value = parser.readValueAs(String.class);
        return deserializeBase64(value);
    }

    @SuppressWarnings("java:S1168") //we need to return null to potentially ignore the field in the JSON
    protected byte[] deserializeBase64(@Nullable final String value) {
        final var optional = Optional.ofNullable(value);
        return optional
                .filter(StringUtils::hasText)
                .map(decoder::decode)
                .orElse(new byte[0]);
    }
}
