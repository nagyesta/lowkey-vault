package com.github.nagyesta.lowkeyvault.http.management;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

public class EpochSecondsDeserializer
        extends JsonDeserializer<OffsetDateTime> {

    @Override
    public @Nullable OffsetDateTime deserialize(
            final JsonParser parser,
            final DeserializationContext context) throws IOException {
        return Optional.of(parser.readValueAs(Long.class))
                .map(Instant::ofEpochSecond)
                .map((Instant instant) -> OffsetDateTime.ofInstant(instant, ZoneOffset.UTC))
                .orElse(null);
    }
}
