package com.github.nagyesta.lowkeyvault.model.json.util;

import com.github.nagyesta.lowkeyvault.service.certificate.CertificateLifetimeActionActivity;
import org.springframework.util.Assert;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

public class CertificateLifetimeActionDeserializer
        extends ValueDeserializer<CertificateLifetimeActionActivity> {

    private static final String INNER_NODE_NAME = "action_type";

    @Override
    public CertificateLifetimeActionActivity deserialize(
            final JsonParser parser,
            final DeserializationContext context) {
        final var node = parser.readValueAsTree();
        Assert.isTrue(node.isObject(), "The \"action\" node must represent an object.");
        final var actionType = node.path(INNER_NODE_NAME);
        Assert.isTrue(actionType.isValueNode(),
                "The \"action\" node must have an \"" + INNER_NODE_NAME + "\" child containing the value.");
        try (var textField = actionType.traverse(context)) {
            final var value = textField.nextStringValue();
            return CertificateLifetimeActionActivity.byValue(value);
        }
    }
}
