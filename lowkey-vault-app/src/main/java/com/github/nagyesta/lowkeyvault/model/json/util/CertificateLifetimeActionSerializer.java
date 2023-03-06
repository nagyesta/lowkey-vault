package com.github.nagyesta.lowkeyvault.model.json.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.github.nagyesta.lowkeyvault.service.certificate.CertificateLifetimeActionActivity;

import java.io.IOException;

public class CertificateLifetimeActionSerializer extends JsonSerializer<CertificateLifetimeActionActivity> {

    @Override
    public void serialize(final CertificateLifetimeActionActivity value,
                          final JsonGenerator generator,
                          final SerializerProvider provider) throws IOException {
        generator.writeStartObject();
        generator.writeStringField("action_type", value.getValue());
        generator.writeEndObject();
    }
}
