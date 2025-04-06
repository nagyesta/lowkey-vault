package com.github.nagyesta.lowkeyvault.model.json.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.nagyesta.lowkeyvault.model.common.backup.KeyBackupList;

public class Base64ZipKeyDeserializer
        extends AbstractBase64ZipDeserializer<KeyBackupList> {

    public Base64ZipKeyDeserializer() {
        this(new Base64Deserializer(), new ObjectMapper().findAndRegisterModules());
    }

    protected Base64ZipKeyDeserializer(
            final Base64Deserializer base64Deserializer,
            final ObjectMapper objectMapper) {
        super(base64Deserializer, objectMapper);
    }

    @Override
    protected Class<KeyBackupList> getType() {
        return KeyBackupList.class;
    }
}
