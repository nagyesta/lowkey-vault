package com.github.nagyesta.lowkeyvault.service.certificate.id;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.github.nagyesta.lowkeyvault.TestConstantsCertificates.CERT_NAME_1;
import static com.github.nagyesta.lowkeyvault.TestConstantsUri.HTTPS_LOWKEY_VAULT;

class VersionedCertificateEntityIdTest {

    @Test
    void testConstructorWithoutVersionShouldGenerateVersionWhenCalledWithoutValidInput() {
        //given

        //when
        final var actual = new VersionedCertificateEntityId(HTTPS_LOWKEY_VAULT, CERT_NAME_1);

        //then
        Assertions.assertNotNull(actual.version());
    }
}
