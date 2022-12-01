package com.github.nagyesta.lowkeyvault.service.certificate.impl;

import com.github.nagyesta.lowkeyvault.ResourceUtils;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyCurveName;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyType;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.request.JsonWebKeyImportRequest;
import com.github.nagyesta.lowkeyvault.service.exception.CryptoException;
import com.github.nagyesta.lowkeyvault.service.key.util.KeyGenUtil;
import org.bouncycastle.util.encoders.Base64;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.charset.StandardCharsets;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static com.github.nagyesta.lowkeyvault.service.certificate.impl.CertContentType.PEM;

@SuppressWarnings("UnnecessaryLocalVariable")
class CertContentTypeTest {

    private static final String EC_CERT = "MIIBRDCB6aADAgECAgR3UZEbMAwGCCqGSM49BAMCBQAwFzEVMBMGA1UEAxMMZWMu"
            + "bG9jYWxob3N0MB4XDTIyMDkxMDE5MDA1NVoXDTIzMDkxMDE5MDA1NVowFzEVMBMG"
            + "A1UEAxMMZWMubG9jYWxob3N0MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAE9QeN"
            + "Y5gGwMQnCSUrFfJ1CQp8ngrTJn9ZzTusUY8Gh5JWennjFdzLIqJ4yhpSzGAl+/jn"
            + "Gv3n+fBjt7mUZu2I76MhMB8wHQYDVR0OBBYEFA2YDS/W2/Dv5qJrmtbE7w+HUtL0"
            + "MAwGCCqGSM49BAMCBQADSAAwRQIgMQAYrmTDkcxQgS33oHbw+H/7YEO43ZDqSOTr"
            + "tn7PQa8CIQCf8JCfvoC0W67JsBRFPDNJEKBuNHVMOWuKjwrXaqynuQ==";
    private static final String RSA_CERT = "MIIDUzCCAjugAwIBAgIQZ03r0foFSbuyMPZn6+/irDANBgkqhkiG9w0BAQsFADAU"
            + "MRIwEAYDVQQDEwlsb2NhbGhvc3QwIBcNMjIwODI4MTIzMDQzWhgPMjA1MjA4Mjgx"
            + "MjQwNDNaMBQxEjAQBgNVBAMTCWxvY2FsaG9zdDCCASIwDQYJKoZIhvcNAQEBBQAD"
            + "ggEPADCCAQoCggEBALXdCn8hSkdTXLfEGYdwDfvEfPRl+wipthTDtYVZ6AJGjG53"
            + "mu/nRuf1rFG4W5OXTFV/WL5pbDOsNvvV1PlUG/+VRBVO/r5D0AmHJjVyflozKIhB"
            + "RDx2GgM8pTgpoEVuzJG/pb8Up+kQiCEUjJ1TAf3gojl59lEFatQNsWbHnGvV3xmK"
            + "RpECzBQipRjMi+4U3/9ebgrY91UDj+/tkqK4SWqxeb9qE5H41CHEJXkpgnGJRZFw"
            + "1IDcJntoW+973msI69S5GZZ3ICzLpAeTm1CZyVj/Kn4xn/ag8tHzNAmfhA7gQDtO"
            + "2vVYuF4IKLF5YCI2jNcTeyk0ZPXljgJ9RD8486ECAwEAAaOBnjCBmzAOBgNVHQ8B"
            + "Af8EBAMCBaAwCQYDVR0TBAIwADAdBgNVHSUEFjAUBggrBgEFBQcDAQYIKwYBBQUH"
            + "AwIwHwYDVR0RBBgwFoIJbG9jYWxob3N0ggkxMjcuMC4wLjEwHwYDVR0jBBgwFoAU"
            + "LnKkDUiyqtO+WmZSPeYyhcwSZHMwHQYDVR0OBBYEFC5ypA1IsqrTvlpmUj3mMoXM"
            + "EmRzMA0GCSqGSIb3DQEBCwUAA4IBAQCqTxssPDFRHW1GzA43gph52bY8ZLuSFOcb"
            + "2p98T9STkPzudq9Pqha4n5/N9AIIYpNU/BFGQMvgilmJK1e0r5BqACTZ+xw4Zm92"
            + "KLMVLeVS6mGLKYklJvwjFblfJtjN++l5j5coMGiWgVLQTJhyFwHtWSdh1J0DNbwE"
            + "/eGSDWHJ20KDyt98c7QJIjt87KIh3jd1WRzeRZ7YWWdRxigerYlupO2iFSr28seB"
            + "NjuCqPwdGwuYHGe/SskEqjVYxFoFknPhsn5Y64b1RuJe19qjewYl0NBmBjiEexY1"
            + "Tg/nnzqHPv4GAnWcp4e9IOAB00LfXwFj4D/lTOuGpdUFeIhjN0dx";

    public static Stream<Arguments> instanceProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(CertContentType.PKCS12))
                .add(Arguments.of(PEM))
                .build();
    }

    @Test
    void testGetMimeTypeShouldReturnTheMimeTypeOfTheSourceWhenCalledOnPkcs12() {
        //given
        final CertContentType underTest = CertContentType.PKCS12;

        //when
        final String actual = underTest.getMimeType();

        //then
        Assertions.assertEquals("application/x-pkcs12", actual);
    }

    @Test
    void testGetMimeTypeShouldReturnTheMimeTypeOfTheSourceWhenCalledOnPem() {
        //given
        final CertContentType underTest = PEM;

        //when
        final String actual = underTest.getMimeType();

        //then
        Assertions.assertEquals("application/x-pem-file", actual);
    }

    @Test
    void testGetCertificateChainShouldThrowExceptionWhenPemCalledWithPkcs12Store() {
        //given
        final String store = ResourceUtils.loadResourceAsBase64String("/cert/ec.p12");
        final CertContentType underTest = CertContentType.PEM;

        //when
        Assertions.assertThrows(CryptoException.class, () -> underTest.getCertificateChain(store, "changeit"));

        //then + exception
    }

    @Test
    void testGetCertificateChainShouldThrowExceptionWhenPkcs12CalledWithPemStore() {
        //given
        final String store = ResourceUtils.loadResourceAsString("/cert/ec.pem");
        final CertContentType underTest = CertContentType.PKCS12;

        //when
        Assertions.assertThrows(CryptoException.class, () -> underTest.getCertificateChain(store, "changeit"));

        //then + exception
    }

    @Test
    void testGetCertificateChainShouldLoadCertificateChainWhenCalledWithEcPkcs12Store() throws CertificateEncodingException {
        //given
        final String store = ResourceUtils.loadResourceAsBase64String("/cert/ec.p12");
        final CertContentType underTest = CertContentType.PKCS12;

        //when
        final List<Certificate> actual = underTest.getCertificateChain(store, "changeit");

        //then
        Assertions.assertEquals(1, actual.size());
        final Certificate certificate = actual.get(0);
        Assertions.assertEquals("X.509", certificate.getType());
        Assertions.assertEquals(EC_CERT, toBase64(certificate));
    }

    @Test
    void testGetCertificateChainShouldLoadCertificateChainWhenCalledWithEcPemStore() throws CertificateEncodingException {
        //given
        final String store = ResourceUtils.loadResourceAsString("/cert/ec.pem");
        final CertContentType underTest = PEM;

        //when
        final List<Certificate> actual = underTest.getCertificateChain(store, "changeit");

        //then
        Assertions.assertEquals(1, actual.size());
        final Certificate certificate = actual.get(0);
        Assertions.assertEquals("X.509", certificate.getType());
        Assertions.assertEquals(EC_CERT, toBase64(certificate));
    }

    @Test
    void testGetKeyShouldThrowExceptionWhenPemCalledWithPkcs12Store() {
        //given
        final String store = ResourceUtils.loadResourceAsBase64String("/cert/ec.p12");
        final CertContentType underTest = CertContentType.PEM;

        //when
        Assertions.assertThrows(CryptoException.class, () -> underTest.getKey(store, "changeit"));

        //then + exception
    }

    @Test
    void testGetKeyShouldThrowExceptionWhenPkcs12CalledWithPemStore() {
        //given
        final String store = ResourceUtils.loadResourceAsString("/cert/ec.pem");
        final CertContentType underTest = CertContentType.PKCS12;

        //when
        Assertions.assertThrows(CryptoException.class, () -> underTest.getKey(store, "changeit"));

        //then + exception
    }

    @Test
    void testGetKeyShouldParseKeyParametersWhenCalledWithEcPkcs12Store() {
        //given
        final String store = ResourceUtils.loadResourceAsBase64String("/cert/ec.p12");
        final CertContentType underTest = CertContentType.PKCS12;

        //when
        final JsonWebKeyImportRequest actual = underTest.getKey(store, "changeit");

        //then
        Assertions.assertEquals(KeyType.EC, actual.getKeyType());
        Assertions.assertEquals(KeyCurveName.P_256, actual.getCurveName());
    }

    @Test
    void testGetKeyShouldParseKeyParametersWhenCalledWithEcPemStore() {
        //given
        final String store = ResourceUtils.loadResourceAsString("/cert/ec.pem");
        final CertContentType underTest = PEM;

        //when
        final JsonWebKeyImportRequest actual = underTest.getKey(store, "changeit");

        //then
        Assertions.assertEquals(KeyType.EC, actual.getKeyType());
        Assertions.assertEquals(KeyCurveName.P_256, actual.getCurveName());
    }

    @Test
    void testGetCertificateChainShouldLoadCertificateChainWhenCalledWithRsaPkcs12Store() throws CertificateEncodingException {
        //given
        final String store = ResourceUtils.loadResourceAsBase64String("/cert/rsa.p12");
        final CertContentType underTest = CertContentType.PKCS12;

        //when
        final List<Certificate> actual = underTest.getCertificateChain(store, "");

        //then
        Assertions.assertEquals(1, actual.size());
        final Certificate certificate = actual.get(0);
        Assertions.assertEquals("X.509", certificate.getType());
        Assertions.assertEquals(RSA_CERT, toBase64(certificate));
    }

    @Test
    void testGetCertificateChainShouldLoadCertificateChainWhenCalledWithRsaPemStore() throws CertificateEncodingException {
        //given
        final String store = ResourceUtils.loadResourceAsString("/cert/rsa.pem");
        final CertContentType underTest = PEM;

        //when
        final List<Certificate> actual = underTest.getCertificateChain(store, "changeit");

        //then
        Assertions.assertEquals(1, actual.size());
        final Certificate certificate = actual.get(0);
        Assertions.assertEquals("X.509", certificate.getType());
        Assertions.assertEquals(RSA_CERT, toBase64(certificate));
    }

    @Test
    void testGetKeyShouldParseKeyParametersWhenCalledWithRsaPkcs12Store() {
        //given
        final String store = ResourceUtils.loadResourceAsBase64String("/cert/rsa.p12");
        final CertContentType underTest = CertContentType.PKCS12;

        //when
        final JsonWebKeyImportRequest actual = underTest.getKey(store, "");

        //then
        Assertions.assertEquals(KeyType.RSA, actual.getKeyType());
    }

    @Test
    void testGetKeyShouldParseKeyParametersWhenCalledWithRsaPemStore() {
        //given
        final String store = ResourceUtils.loadResourceAsString("/cert/rsa.pem");
        final CertContentType underTest = PEM;

        //when
        final JsonWebKeyImportRequest actual = underTest.getKey(store, "changeit");

        //then
        Assertions.assertEquals(KeyType.RSA, actual.getKeyType());
    }

    @ParameterizedTest
    @MethodSource("instanceProvider")
    void testGetKeyShouldThrowExceptionWhenCalledWithNullCertificate(final CertContentType underTest) {
        //given

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.getKey(null, "changeit"));

        //then + exception
    }

    @ParameterizedTest
    @MethodSource("instanceProvider")
    void testGetKeyShouldThrowExceptionWhenCalledWithNullPassword(final CertContentType underTest) {
        //given
        final String store = ResourceUtils.loadResourceAsString("/cert/rsa.pem");

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.getKey(store, null));

        //then + exception
    }

    @ParameterizedTest
    @MethodSource("instanceProvider")
    void testGetCertificateChainShouldThrowExceptionWhenCalledWithNullCertificate(final CertContentType underTest) {
        //given

        //when
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> underTest.getCertificateChain(null, "changeit"));

        //then + exception
    }

    @ParameterizedTest
    @MethodSource("instanceProvider")
    void testGetCertificateChainShouldThrowExceptionWhenCalledWithNullPassword(final CertContentType underTest) {
        //given
        final String store = ResourceUtils.loadResourceAsString("/cert/rsa.pem");

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.getCertificateChain(store, null));

        //then + exception
    }

    @ParameterizedTest
    @MethodSource("instanceProvider")
    void testAsBase64CertificatePackageShouldThrowExceptionWhenCalledWithNullKeyPair(final CertContentType underTest) {
        //given
        final String store = ResourceUtils.loadResourceAsString("/cert/rsa.pem");
        final List<Certificate> chain = CertContentType.PEM.getCertificateChain(Objects.requireNonNull(store), "");

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.asBase64CertificatePackage(chain.get(0), null));

        //then + exception
    }

    @ParameterizedTest
    @MethodSource("instanceProvider")
    void testAsBase64CertificatePackageShouldThrowExceptionWhenCalledWithNullCertificate(final CertContentType underTest) {
        //given

        //when
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> underTest.asBase64CertificatePackage(null, KeyGenUtil.generateEc(KeyCurveName.P_521)));

        //then + exception
    }

    @ParameterizedTest
    @MethodSource("instanceProvider")
    void testByMimeTypeShouldFindInstanceWhenCalledWithValidInput(final CertContentType expected) {
        //given

        //when
        final CertContentType actual = CertContentType.byMimeType(expected.getMimeType());

        //then
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void testByMimeTypeShouldReturnDefaultWhenCalledWithNull() {
        //given

        //when
        final CertContentType actual = CertContentType.byMimeType(null);

        //then
        Assertions.assertEquals(PEM, actual);
    }

    private static String toBase64(final Certificate certificate) throws CertificateEncodingException {
        return new String(Base64.encode(certificate.getEncoded()), StandardCharsets.UTF_8);
    }

}
