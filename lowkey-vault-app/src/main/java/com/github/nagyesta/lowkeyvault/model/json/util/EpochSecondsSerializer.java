package com.github.nagyesta.lowkeyvault.model.json.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Optional;

public class EpochSecondsSerializer extends JsonSerializer<OffsetDateTime> {

    @Override
    public void serialize(final OffsetDateTime value, final JsonGenerator generator, final SerializerProvider provider) throws IOException {
        final Optional<OffsetDateTime> optional = Optional.ofNullable(value);
        if (optional.isPresent()) {
            final long epochSeconds = optional
                    .map(OffsetDateTime::toEpochSecond)
                    .get();
            generator.writeNumber(epochSeconds);
        } else {
            generator.writeNull();
        }
    }
}
