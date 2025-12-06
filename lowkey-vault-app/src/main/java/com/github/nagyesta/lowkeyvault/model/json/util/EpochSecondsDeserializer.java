package com.github.nagyesta.lowkeyvault.model.json.util;

import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

public class EpochSecondsDeserializer
        extends ValueDeserializer<OffsetDateTime> {

    @Override
    public OffsetDateTime deserialize(
            final JsonParser parser,
            final DeserializationContext context) {
        return Optional.ofNullable(parser.readValueAs(Long.class))
                .map(Instant::ofEpochSecond)
                .map((Instant instant) -> OffsetDateTime.ofInstant(instant, ZoneOffset.UTC))
                .orElse(null);
    }
}
