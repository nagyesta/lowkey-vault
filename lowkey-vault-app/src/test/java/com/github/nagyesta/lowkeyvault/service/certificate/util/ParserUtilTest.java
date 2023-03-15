package com.github.nagyesta.lowkeyvault.service.certificate.util;

import com.github.nagyesta.lowkeyvault.service.certificate.impl.CertAuthorityType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.security.auth.x500.X500Principal;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.security.cert.X509Certificate;
import java.time.OffsetDateTime;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.github.nagyesta.lowkeyvault.TestConstants.NOW;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ParserUtilTest {

    @SuppressWarnings("checkstyle:MagicNumber")
    public static Stream<Arguments> monthProvider() {
        return IntStream.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 24, 36, 42, 120, 240, 360)
                .mapToObj(Arguments::of);
    }

    public static Stream<Arguments> issuerAndSubjectProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of("CN=localhost", "CN=localhost", CertAuthorityType.SELF_SIGNED))
                .add(Arguments.of("CN=example.com", "CN=example.com", CertAuthorityType.SELF_SIGNED))
                .add(Arguments.of("CN=example.com", "CN=localhost", CertAuthorityType.UNKNOWN))
                .build();
    }

    @Test
    void testConstructorShouldThrowExceptionWhenCalled() throws NoSuchMethodException {
        //given
        final Constructor<ParserUtil> constructor = ParserUtil.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        //when
        Assertions.assertThrows(InvocationTargetException.class, constructor::newInstance);

        //then + exception
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    @ParameterizedTest
    @MethodSource("monthProvider")
    void testCalculateValidityMonthsShouldReturnExpectedNumberWhenCalledWithValidValues(final int realMonths) {
        //given
        final OffsetDateTime from = NOW;
        final OffsetDateTime to = NOW.plusMonths(realMonths);

        //when
        final int actual = ParserUtil.calculateValidityMonths(from.toInstant(), to.toInstant());

        //then
        Assertions.assertEquals(realMonths, actual);
    }

    @ParameterizedTest
    @MethodSource("issuerAndSubjectProvider")
    void testParseCertAuthorityTypeShouldCompareIssuerAndSubjectWhenCalledWithValidCertificate(
            final String issuer, final String subject, final CertAuthorityType expected) {
        //given
        final X500Principal issuerX500Principal = new X500Principal(issuer);
        final X500Principal subjectX500Principal = new X500Principal(subject);
        final X509Certificate certificate = mock(X509Certificate.class);
        when(certificate.getIssuerX500Principal()).thenReturn(issuerX500Principal);
        when(certificate.getSubjectX500Principal()).thenReturn(subjectX500Principal);

        //when
        final CertAuthorityType actual = ParserUtil.parseCertAuthorityType(certificate);

        //then
        Assertions.assertEquals(expected, actual);
    }

    @SuppressWarnings("DataFlowIssue")
    @Test
    void testParseCertAuthorityTypeShouldThrowExceptionWhenCalledWithNull() {
        //given

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> ParserUtil.parseCertAuthorityType(null));

        //then + exception
    }
}
