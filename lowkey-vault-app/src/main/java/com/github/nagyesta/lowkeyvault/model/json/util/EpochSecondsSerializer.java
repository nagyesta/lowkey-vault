package com.github.nagyesta.lowkeyvault.model.json.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Optional;

public class EpochSecondsSerializer
        extends JsonSerializer<OffsetDateTime> {

    @Override
    public void serialize(
            final OffsetDateTime value,
            final JsonGenerator generator,
            final SerializerProvider provider) throws IOException {
        final var optional = Optional.ofNullable(value);
        final var optionalEpochSeconds = optional
                .map(OffsetDateTime::toEpochSecond);
        if (optionalEpochSeconds.isPresent()) {
            generator.writeNumber(optionalEpochSeconds.get());
        } else {
            generator.writeNull();
        }
    }
}
