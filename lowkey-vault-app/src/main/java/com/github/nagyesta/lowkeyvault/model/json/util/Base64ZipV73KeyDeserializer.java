package com.github.nagyesta.lowkeyvault.model.json.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.nagyesta.lowkeyvault.model.v7_3.key.KeyBackupList;

public class Base64ZipV73KeyDeserializer extends AbstractBase64ZipDeserializer<KeyBackupList> {

    public Base64ZipV73KeyDeserializer() {
        this(new Base64Deserializer(), new ObjectMapper().findAndRegisterModules());
    }

    protected Base64ZipV73KeyDeserializer(final Base64Deserializer base64Deserializer, final ObjectMapper objectMapper) {
        super(base64Deserializer, objectMapper);
    }

    @Override
    protected Class<KeyBackupList> getType() {
        return KeyBackupList.class;
    }
}
