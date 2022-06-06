package com.github.nagyesta.lowkeyvault.model.json.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.KeyBackupList;

public class Base64ZipV72KeySerializer extends AbstractBase64ZipSerializer<KeyBackupList> {

    public Base64ZipV72KeySerializer() {
        this(new Base64Serializer(), new ObjectMapper());
    }

    protected Base64ZipV72KeySerializer(final Base64Serializer base64Serializer, final ObjectMapper objectMapper) {
        super(base64Serializer, objectMapper);
    }
}
