package com.github.nagyesta.lowkeyvault.model.json.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.github.nagyesta.lowkeyvault.service.certificate.CertificateLifetimeActionActivity;
import org.springframework.util.Assert;

import java.io.IOException;

public class CertificateLifetimeActionDeserializer extends JsonDeserializer<CertificateLifetimeActionActivity> {

    private static final String INNER_NODE_NAME = "action_type";

    @Override
    public CertificateLifetimeActionActivity deserialize(final JsonParser parser, final DeserializationContext context) throws IOException {
        final TreeNode node = parser.readValueAsTree();
        Assert.isTrue(node.isObject(), "The \"action\" node must represent an object.");
        final TreeNode actionType = node.path(INNER_NODE_NAME);
        Assert.isTrue(actionType.isValueNode(),
                "The \"action\" node must have an \"" + INNER_NODE_NAME + "\" child containing the value.");
        try (JsonParser textField = actionType.traverse()) {
            final String value = textField.nextTextValue();
            return CertificateLifetimeActionActivity.byValue(value);
        }
    }
}
