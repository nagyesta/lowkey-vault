package com.github.nagyesta.lowkeyvault.service.certificate.id;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.net.URI;
import java.util.stream.Stream;

import static com.github.nagyesta.lowkeyvault.TestConstantsCertificates.*;
import static com.github.nagyesta.lowkeyvault.TestConstantsUri.*;

class CertificateEntityIdTest {

    public static Stream<Arguments> vaultProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(UNVERSIONED_CERT_ENTITY_ID_1, HTTPS_LOCALHOST_8443))
                .add(Arguments.of(UNVERSIONED_CERT_ENTITY_ID_2, HTTPS_LOWKEY_VAULT))
                .add(Arguments.of(UNVERSIONED_CERT_ENTITY_ID_3, HTTPS_LOCALHOST_8443))
                .add(Arguments.of(VERSIONED_CERT_ENTITY_ID_1_VERSION_1, HTTPS_LOCALHOST_8443))
                .build();
    }

    public static Stream<Arguments> idProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(UNVERSIONED_CERT_ENTITY_ID_1, CERT_NAME_1))
                .add(Arguments.of(UNVERSIONED_CERT_ENTITY_ID_2, CERT_NAME_2))
                .add(Arguments.of(UNVERSIONED_CERT_ENTITY_ID_3, CERT_NAME_3))
                .add(Arguments.of(VERSIONED_CERT_ENTITY_ID_1_VERSION_1, CERT_NAME_1))
                .build();
    }

    public static Stream<Arguments> versionProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(VERSIONED_CERT_ENTITY_ID_1_VERSION_2, CERT_VERSION_2))
                .add(Arguments.of(VERSIONED_CERT_ENTITY_ID_2_VERSION_1, CERT_VERSION_1))
                .add(Arguments.of(VERSIONED_CERT_ENTITY_ID_3_VERSION_3, CERT_VERSION_3))
                .add(Arguments.of(VERSIONED_CERT_ENTITY_ID_1_VERSION_3, CERT_VERSION_3))
                .build();
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    @Test
    void testEntityTypeShouldReturnThePredefinedLiteralWhenCalled() {
        //given
        final var underTest = UNVERSIONED_CERT_ENTITY_ID_1;

        //when
        final var actual = underTest.entityType();

        //then
        Assertions.assertEquals("certificate", actual);
    }

    @ParameterizedTest
    @MethodSource("vaultProvider")
    void testVaultShouldReturnTheVaultUriWhenCalled(
            final CertificateEntityId underTest,
            final URI expected) {
        //given

        //when
        final var actual = underTest.vault();

        //then
        Assertions.assertEquals(expected, actual);
    }

    @ParameterizedTest
    @MethodSource("idProvider")
    void testIdShouldReturnTheCertNameWhenCalled(
            final CertificateEntityId underTest,
            final String expected) {
        //given

        //when
        final var actual = underTest.id();

        //then
        Assertions.assertEquals(expected, actual);
    }

    @ParameterizedTest
    @MethodSource("versionProvider")
    void testVersionShouldReturnTheCertNameWhenCalled(
            final CertificateEntityId underTest,
            final String expected) {
        //given

        //when
        final var actual = underTest.version();

        //then
        Assertions.assertEquals(expected, actual);
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    @Test
    void testAsUriNoVersionShouldReturnUriUsingAliasWhenCalledWithValidAlias() {
        //given
        final var underTest = UNVERSIONED_CERT_ENTITY_ID_1;

        //when
        final var actual = underTest.asUriNoVersion(HTTPS_LOOP_BACK_IP);

        //then
        final var expected = HTTPS_LOOP_BACK_IP + "/certificates/" + CERT_NAME_1;
        Assertions.assertEquals(expected, actual.toString());
    }

    @SuppressWarnings({"UnnecessaryLocalVariable", "ConstantConditions"})
    @Test
    void testAsUriNoVersionShouldThrowExceptionWhenCalledWithNullAlias() {
        //given
        final CertificateEntityId underTest = VERSIONED_CERT_ENTITY_ID_1_VERSION_3;

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.asUriNoVersion(null));

        //then + exception
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    @Test
    void testAsUriShouldReturnUriUsingAliasWhenCalledWithValidAlias() {
        //given
        final CertificateEntityId underTest = VERSIONED_CERT_ENTITY_ID_1_VERSION_3;

        //when
        final var actual = underTest.asUri(HTTPS_LOOP_BACK_IP);

        //then
        final var expected = HTTPS_LOOP_BACK_IP + "/certificates/" + CERT_NAME_1 + "/" + CERT_VERSION_3;
        Assertions.assertEquals(expected, actual.toString());
    }

    @SuppressWarnings({"UnnecessaryLocalVariable", "ConstantConditions"})
    @Test
    void testAsUriShouldThrowExceptionWhenCalledWithNullAlias() {
        //given
        final CertificateEntityId underTest = VERSIONED_CERT_ENTITY_ID_1_VERSION_3;

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.asUri(null));

        //then + exception
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    @Test
    void testAsUriWithQueryShouldReturnUriUsingAliasWhenCalledWithValidAlias() {
        //given
        final CertificateEntityId underTest = VERSIONED_CERT_ENTITY_ID_1_VERSION_3;
        final var query = "?query=1";

        //when
        final var actual = underTest.asUri(HTTPS_LOOP_BACK_IP, query);

        //then
        final var expected = HTTPS_LOOP_BACK_IP + "/certificates/" + CERT_NAME_1 + "/" + CERT_VERSION_3 + query;
        Assertions.assertEquals(expected, actual.toString());
    }

    @SuppressWarnings({"UnnecessaryLocalVariable", "ConstantConditions"})
    @Test
    void testAsUriWithQueryShouldThrowExceptionWhenCalledWithNullAlias() {
        //given
        final CertificateEntityId underTest = VERSIONED_CERT_ENTITY_ID_1_VERSION_3;

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.asUri(null, "?query=1"));

        //then + exception
    }

    @SuppressWarnings({"UnnecessaryLocalVariable", "ConstantConditions"})
    @Test
    void testAsUriWithQueryShouldThrowExceptionWhenCalledWithNullQuery() {
        //given
        final CertificateEntityId underTest = VERSIONED_CERT_ENTITY_ID_1_VERSION_3;

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.asUri(HTTPS_LOCALHOST_8443, null));

        //then + exception
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    @Test
    void testAsRecoveryUriShouldReturnUriUsingAliasWhenCalledWithValidAlias() {
        //given
        final CertificateEntityId underTest = VERSIONED_CERT_ENTITY_ID_1_VERSION_3;

        //when
        final var actual = underTest.asRecoveryUri(HTTPS_LOOP_BACK_IP);

        //then
        final var expected = HTTPS_LOOP_BACK_IP + "/deletedcertificates/" + CERT_NAME_1;
        Assertions.assertEquals(expected, actual.toString());
    }

    @SuppressWarnings({"UnnecessaryLocalVariable", "ConstantConditions"})
    @Test
    void testAsRecoveryUriShouldThrowExceptionWhenCalledWithNullAlias() {
        //given
        final CertificateEntityId underTest = VERSIONED_CERT_ENTITY_ID_1_VERSION_3;

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.asRecoveryUri(null));

        //then + exception
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    @Test
    void testAsPolicyUriShouldReturnUriUsingAliasWhenCalledWithValidAlias() {
        //given
        final CertificateEntityId underTest = VERSIONED_CERT_ENTITY_ID_1_VERSION_3;

        //when
        final var actual = underTest.asPolicyUri(HTTPS_LOOP_BACK_IP);

        //then
        final var expected = HTTPS_LOOP_BACK_IP + "/certificates/" + CERT_NAME_1 + "/" + CERT_VERSION_3 + "/policy";
        Assertions.assertEquals(expected, actual.toString());
    }

    @SuppressWarnings({"UnnecessaryLocalVariable", "ConstantConditions"})
    @Test
    void testAsPolicyUriShouldThrowExceptionWhenCalledWithNullAlias() {
        //given
        final CertificateEntityId underTest = VERSIONED_CERT_ENTITY_ID_1_VERSION_3;

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.asPolicyUri(null));

        //then + exception
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    @Test
    void testAsPendingOperationUriShouldReturnUriUsingAliasWhenCalledWithValidAlias() {
        //given
        final CertificateEntityId underTest = VERSIONED_CERT_ENTITY_ID_1_VERSION_3;

        //when
        final var actual = underTest.asPendingOperationUri(HTTPS_LOOP_BACK_IP);

        //then
        final var expected = HTTPS_LOOP_BACK_IP + "/certificates/" + CERT_NAME_1 + "/pending";
        Assertions.assertEquals(expected, actual.toString());
    }

    @SuppressWarnings({"UnnecessaryLocalVariable", "ConstantConditions"})
    @Test
    void testAsPendingOperationUriShouldThrowExceptionWhenCalledWithNullAlias() {
        //given
        final CertificateEntityId underTest = VERSIONED_CERT_ENTITY_ID_1_VERSION_3;

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.asPendingOperationUri(null));

        //then + exception
    }

}
