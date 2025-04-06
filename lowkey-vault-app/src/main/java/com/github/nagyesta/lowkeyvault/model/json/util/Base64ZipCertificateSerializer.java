package com.github.nagyesta.lowkeyvault.model.json.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.nagyesta.lowkeyvault.model.common.backup.CertificateBackupList;

public class Base64ZipCertificateSerializer
        extends AbstractBase64ZipSerializer<CertificateBackupList> {

    @SuppressWarnings("unused") //used from annotations
    public Base64ZipCertificateSerializer() {
        this(new Base64Serializer(), new ObjectMapper());
    }

    protected Base64ZipCertificateSerializer(
            final Base64Serializer base64Serializer,
            final ObjectMapper objectMapper) {
        super(base64Serializer, objectMapper);
    }
}
