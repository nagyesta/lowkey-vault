package com.github.nagyesta.lowkeyvault.model.json.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

public class EpochSecondsDeserializer
        extends JsonDeserializer<OffsetDateTime> {

    @Override
    public OffsetDateTime deserialize(
            final JsonParser parser,
            final DeserializationContext context) throws IOException {
        return Optional.ofNullable(parser.readValueAs(Long.class))
                .map(Instant::ofEpochSecond)
                .map((Instant instant) -> OffsetDateTime.ofInstant(instant, ZoneOffset.UTC))
                .orElse(null);
    }
}
