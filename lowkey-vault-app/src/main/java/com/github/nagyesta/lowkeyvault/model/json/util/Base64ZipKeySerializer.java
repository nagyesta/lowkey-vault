package com.github.nagyesta.lowkeyvault.model.json.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.nagyesta.lowkeyvault.model.common.backup.KeyBackupList;

public class Base64ZipKeySerializer
        extends AbstractBase64ZipSerializer<KeyBackupList> {

    public Base64ZipKeySerializer() {
        this(new Base64Serializer(), new ObjectMapper().findAndRegisterModules());
    }

    protected Base64ZipKeySerializer(
            final Base64Serializer base64Serializer,
            final ObjectMapper objectMapper) {
        super(base64Serializer, objectMapper);
    }
}
