package com.github.nagyesta.lowkeyvault.model.json.util;

import com.github.nagyesta.lowkeyvault.model.common.backup.CertificateBackupList;
import tools.jackson.databind.ObjectMapper;

public class Base64ZipCertificateDeserializer extends AbstractBase64ZipDeserializer<CertificateBackupList> {

    @SuppressWarnings("unused") //used from annotations
    public Base64ZipCertificateDeserializer() {
        this(new Base64Deserializer(), new ObjectMapper());
    }

    protected Base64ZipCertificateDeserializer(
            final Base64Deserializer base64Deserializer,
            final ObjectMapper objectMapper) {
        super(base64Deserializer, objectMapper);
    }

    @Override
    protected Class<CertificateBackupList> getType() {
        return CertificateBackupList.class;
    }
}
