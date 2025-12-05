package com.github.nagyesta.lowkeyvault.model.json.util;

import com.github.nagyesta.lowkeyvault.model.common.backup.KeyBackupList;
import tools.jackson.databind.ObjectMapper;

public class Base64ZipKeySerializer
        extends AbstractBase64ZipSerializer<KeyBackupList> {

    public Base64ZipKeySerializer() {
        this(new Base64Serializer(), new ObjectMapper());
    }

    protected Base64ZipKeySerializer(
            final Base64Serializer base64Serializer,
            final ObjectMapper objectMapper) {
        super(base64Serializer, objectMapper);
    }
}
