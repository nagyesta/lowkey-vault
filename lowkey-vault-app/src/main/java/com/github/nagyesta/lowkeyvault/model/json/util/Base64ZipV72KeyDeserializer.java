package com.github.nagyesta.lowkeyvault.model.json.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.KeyBackupList;

public class Base64ZipV72KeyDeserializer extends AbstractBase64ZipDeserializer<KeyBackupList> {

    public Base64ZipV72KeyDeserializer() {
        this(new Base64Deserializer(), new ObjectMapper());
    }

    protected Base64ZipV72KeyDeserializer(final Base64Deserializer base64Deserializer, final ObjectMapper objectMapper) {
        super(base64Deserializer, objectMapper);
    }

    @Override
    protected Class<KeyBackupList> getType() {
        return KeyBackupList.class;
    }
}
