package com.github.nagyesta.lowkeyvault.model.json.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Base64;
import java.util.Optional;

public class Base64Deserializer extends JsonDeserializer<byte[]> {
    private static final Base64.Decoder DECODER = Base64.getUrlDecoder();

    @Override
    public byte[] deserialize(final JsonParser parser, final DeserializationContext context) throws IOException {
        final Optional<String> optional = Optional.ofNullable(parser.readValueAs(String.class));
        if (optional.isEmpty()) {
            return null;
        }
        return optional
                .filter(StringUtils::hasText)
                .map(DECODER::decode)
                .orElse(new byte[0]);
    }
}
