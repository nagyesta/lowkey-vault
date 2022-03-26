package com.github.nagyesta.lowkeyvault.model.json.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.KeyBackupList;

public class Base64ZipKeyDeserializer extends AbstractBase64ZipDeserializer<KeyBackupList> {

    public Base64ZipKeyDeserializer() {
        this(new Base64Deserializer(), new ObjectMapper());
    }

    protected Base64ZipKeyDeserializer(final Base64Deserializer base64Deserializer, final ObjectMapper objectMapper) {
        super(base64Deserializer, objectMapper);
    }

    @Override
    protected Class<KeyBackupList> getType() {
        return KeyBackupList.class;
    }
}
