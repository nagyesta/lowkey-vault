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
import java.security.KeyPair;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static com.github.nagyesta.lowkeyvault.service.certificate.impl.CertContentType.PEM;
import static com.github.nagyesta.lowkeyvault.service.certificate.impl.CertContentType.PKCS12;
import static org.mockito.Mockito.mock;

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
    private static final int KEY_SIZE = 2048;
    private static final String MIME_BASE64 = "TG9yZW0gaXBzdW0gZG9sb3Igc2l0IGFtZXQsIGNvbnNlY3RldHVyIGFkaXBpc2NpbmcgZWxpdCwg\r\n"
            + "c2VkIGRvIGVpdXNtb2QgdGVtcG9yIGluY2lkaWR1bnQgdXQgbGFib3JlIGV0IGRvbG9yZSBtYWdu\r\n"
            + "YQphbGlxdWEuIFV0IGVuaW0gYWQgbWluaW0gdmVuaWFtLCBxdWlzIG5vc3RydWQgZXhlcmNpdGF0\r\n"
            + "aW9uIHVsbGFtY28gbGFib3JpcyBuaXNpIHV0IGFsaXF1aXAgZXggZWEgY29tbW9kbyBjb25zZXF1\r\n"
            + "YXQuCkR1aXMgYXV0ZSBpcnVyZSBkb2xvciBpbiByZXByZWhlbmRlcml0IGluIHZvbHVwdGF0ZSB2\r\n"
            + "ZWxpdCBlc3NlIGNpbGx1bSBkb2xvcmUgZXUgZnVnaWF0IG51bGxhIHBhcmlhdHVyLiBFeGNlcHRl\r\n"
            + "dXIKc2ludCBvY2NhZWNhdCBjdXBpZGF0YXQgbm9uIHByb2lkZW50LCBzdW50IGluIGN1bHBhIHF1\r\n"
            + "aSBvZmZpY2lhIGRlc2VydW50IG1vbGxpdCBhbmltIGlkIGVzdCBsYWJvcnVtLgo=";
    private static final byte[] MIME_BYTES = ("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt "
            + "ut labore et dolore magna\naliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut "
            + "aliquip ex ea commodo consequat.\nDuis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore "
            + "eu fugiat nulla pariatur. Excepteur\nsint occaecat cupidatat non proident, sunt in culpa qui officia deserunt "
            + "mollit anim id est laborum.\n").getBytes(StandardCharsets.UTF_8);

    public static Stream<Arguments> instanceProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(CertContentType.PKCS12))
                .add(Arguments.of(PEM))
                .build();
    }

    public static Stream<Arguments> base64Provider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of("lorem ipsum".getBytes(StandardCharsets.UTF_8), "bG9yZW0gaXBzdW0="))
                .add(Arguments.of(MIME_BYTES, MIME_BASE64))
                .add(Arguments.of("1".getBytes(StandardCharsets.UTF_8), "MQ=="))
                .add(Arguments.of("12".getBytes(StandardCharsets.UTF_8), "MTI="))
                .add(Arguments.of("123".getBytes(StandardCharsets.UTF_8), "MTIz"))
                .add(Arguments.of("1234".getBytes(StandardCharsets.UTF_8), "MTIzNA=="))
                .add(Arguments.of("12345".getBytes(StandardCharsets.UTF_8), "MTIzNDU="))
                .add(Arguments.of("123456".getBytes(StandardCharsets.UTF_8), "MTIzNDU2"))
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
    void testGetCertificateChainShouldThrowExceptionWhenPemCalledWithInvalidPemStore() {
        //given
        final String store = ResourceUtils.loadResourceAsString("/cert/invalid-ec.pem");
        final CertContentType underTest = CertContentType.PEM;

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
    void testGetKeyShouldThrowExceptionWhenPemCalledWithInvalidPemStore() {
        //given
        final String store = ResourceUtils.loadResourceAsBase64String("/cert/invalid-ec.pem");
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

    @Test
    void testGetKeyShouldThrowExceptionWhenCalledWithNullPasswordAndPkcs12() {
        //given
        final CertContentType underTest = PKCS12;
        final String store = ResourceUtils.loadResourceAsString("/cert/rsa.p12");

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.getKey(store, null));

        //then + exception
    }

    @Test
    void testGetKeyShouldNotThrowExceptionWhenCalledWithNullPasswordAndPem() {
        //given
        final CertContentType underTest = PEM;
        final String store = ResourceUtils.loadResourceAsString("/cert/rsa.pem");

        //when
        Assertions.assertDoesNotThrow(() -> underTest.getKey(store, null));

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

    @Test
    void testGetCertificateChainShouldThrowExceptionWhenCalledWithNullPasswordAndPkcs12() {
        //given
        final CertContentType underTest = PKCS12;
        final String store = ResourceUtils.loadResourceAsString("/cert/rsa.p12");

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.getCertificateChain(store, null));

        //then + exception
    }

    @Test
    void testGetCertificateChainShouldNotThrowExceptionWhenCalledWithNullPasswordAndPem() {
        //given
        final CertContentType underTest = PEM;
        final String store = ResourceUtils.loadResourceAsString("/cert/rsa.pem");

        //when
        Assertions.assertDoesNotThrow(() -> underTest.getCertificateChain(store, null));

        //then + NO exception
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

    @Test
    void testCertificatePackageForBackupShouldThrowExceptionWhenCalledWithNullKeyAndValidCertificateUsingPkcs12Store() {
        //given
        final String store = ResourceUtils.loadResourceAsBase64String("/cert/rsa.p12");
        final Certificate certificate = PKCS12.getCertificateChain(store, "").get(0);
        final CertContentType underTest = PKCS12;

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.certificatePackageForBackup(certificate, null));

        //then + exception
    }

    @Test
    void testCertificatePackageForBackupShouldThrowExceptionWhenCalledWithValidKeyAndNullCertificateUsingPkcs12Store() {
        //given
        final KeyPair keyPair = KeyGenUtil.generateRsa(KEY_SIZE, null);
        final CertContentType underTest = PKCS12;

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.certificatePackageForBackup(null, keyPair));

        //then + exception
    }

    @Test
    void testCertificatePackageForBackupShouldThrowExceptionWhenCalledWithValidKeyAndInvalidCertificateUsingPkcs12Store() {
        //given
        final Certificate certificate = mock(Certificate.class);
        final KeyPair keyPair = KeyGenUtil.generateRsa(KEY_SIZE, null);
        final CertContentType underTest = PKCS12;

        //when
        Assertions.assertThrows(CryptoException.class, () -> underTest.certificatePackageForBackup(certificate, keyPair));

        //then + exception
    }

    @ParameterizedTest
    @MethodSource("base64Provider")
    void testEncodeAsBase64StringShouldProduceTheExpectedBase64String(final byte[] input, final String expected) {
        //given

        //when
        final String actual = CertContentType.encodeAsBase64String(input);

        //then
        Assertions.assertEquals(expected, actual);
    }

    @ParameterizedTest
    @MethodSource("base64Provider")
    void testDecodeBase64StringShouldProduceTheExpectedByteArray(final byte[] expected, final String input) {
        //given

        //when
        final byte[] actual = CertContentType.decodeBase64String(input);

        //then
        Assertions.assertArrayEquals(expected, actual);
    }

    private static String toBase64(final Certificate certificate) throws CertificateEncodingException {
        return new String(Base64.encode(certificate.getEncoded()), StandardCharsets.UTF_8);
    }
}
