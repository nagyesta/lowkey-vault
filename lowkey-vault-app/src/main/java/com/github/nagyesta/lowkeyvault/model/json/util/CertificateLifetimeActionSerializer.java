package com.github.nagyesta.lowkeyvault.model.json.util;

import com.github.nagyesta.lowkeyvault.service.certificate.CertificateLifetimeActionActivity;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

public class CertificateLifetimeActionSerializer
        extends ValueSerializer<CertificateLifetimeActionActivity> {

    @Override
    public void serialize(
            final CertificateLifetimeActionActivity value,
            final JsonGenerator generator,
            final SerializationContext provider) {
        generator.writeStartObject();
        generator.writeStringProperty("action_type", value.getValue());
        generator.writeEndObject();
    }
}
