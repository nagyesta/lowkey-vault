package com.github.nagyesta.lowkeyvault.model.json.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.nagyesta.lowkeyvault.model.common.backup.SecretBackupList;

public class Base64ZipSecretSerializer
        extends AbstractBase64ZipSerializer<SecretBackupList> {

    @SuppressWarnings("unused") //used from annotations
    public Base64ZipSecretSerializer() {
        this(new Base64Serializer(), new ObjectMapper());
    }

    protected Base64ZipSecretSerializer(
            final Base64Serializer base64Serializer,
            final ObjectMapper objectMapper) {
        super(base64Serializer, objectMapper);
    }
}
