package com.github.nagyesta.lowkeyvault.model.json.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.nagyesta.lowkeyvault.model.v7_3.key.KeyBackupList;

public class Base64ZipV73KeySerializer extends AbstractBase64ZipSerializer<KeyBackupList> {

    public Base64ZipV73KeySerializer() {
        this(new Base64Serializer(), new ObjectMapper().findAndRegisterModules());
    }

    protected Base64ZipV73KeySerializer(final Base64Serializer base64Serializer, final ObjectMapper objectMapper) {
        super(base64Serializer, objectMapper);
    }
}
