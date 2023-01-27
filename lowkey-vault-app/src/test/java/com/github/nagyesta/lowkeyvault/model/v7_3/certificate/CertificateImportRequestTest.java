package com.github.nagyesta.lowkeyvault.model.v7_3.certificate;

import com.github.nagyesta.lowkeyvault.ResourceUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;

class CertificateImportRequestTest {

    @Test
    void testGetCertificateAsStringShouldFormatAsBase64WhenCalledWithPkcs12Cert() {
        //given
        final CertificateImportRequest underTest = new CertificateImportRequest();
        final byte[] certContent = Objects.requireNonNull(ResourceUtils.loadResourceAsByteArray("/cert/rsa.p12"));
        underTest.setCertificate(certContent);

        //when
        final String actual = underTest.getCertificateAsString();

        //then
        Assertions.assertArrayEquals(certContent, Base64.getMimeDecoder().decode(actual));
    }

    @Test
    void testGetCertificateAsStringShouldFormatAsBase64WhenCalledWithPemCert() {
        //given
        final CertificateImportRequest underTest = new CertificateImportRequest();
        final byte[] certContent = Objects.requireNonNull(ResourceUtils.loadResourceAsByteArray("/cert/rsa.pem"));
        underTest.setCertificate(certContent);

        //when
        final String actual = underTest.getCertificateAsString();

        //then
        Assertions.assertArrayEquals(certContent, actual.getBytes(StandardCharsets.UTF_8));
    }
}
