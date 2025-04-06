package com.github.nagyesta.lowkeyvault.model.json.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.nagyesta.lowkeyvault.model.common.backup.SecretBackupList;

public class Base64ZipSecretDeserializer
        extends AbstractBase64ZipDeserializer<SecretBackupList> {

    @SuppressWarnings("unused") //used from annotations
    public Base64ZipSecretDeserializer() {
        this(new Base64Deserializer(), new ObjectMapper());
    }

    protected Base64ZipSecretDeserializer(
            final Base64Deserializer base64Deserializer,
            final ObjectMapper objectMapper) {
        super(base64Deserializer, objectMapper);
    }

    @Override
    protected Class<SecretBackupList> getType() {
        return SecretBackupList.class;
    }
}
