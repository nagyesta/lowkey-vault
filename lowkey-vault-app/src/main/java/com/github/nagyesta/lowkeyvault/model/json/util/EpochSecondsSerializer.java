package com.github.nagyesta.lowkeyvault.model.json.util;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

import java.time.OffsetDateTime;
import java.util.Optional;

public class EpochSecondsSerializer
        extends ValueSerializer<OffsetDateTime> {

    @Override
    public void serialize(
            final OffsetDateTime value,
            final JsonGenerator generator,
            final SerializationContext provider) {
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
