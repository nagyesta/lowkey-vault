package com.github.nagyesta.lowkeyvault.service.certificate.impl;

import com.github.nagyesta.lowkeyvault.service.certificate.CertificateLifetimeActionActivity;
import com.github.nagyesta.lowkeyvault.service.certificate.CertificateLifetimeActionTrigger;
import com.github.nagyesta.lowkeyvault.service.certificate.id.CertificateEntityId;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.nagyesta.lowkeyvault.TestConstants.NOW;
import static com.github.nagyesta.lowkeyvault.TestConstantsCertificates.UNVERSIONED_CERT_ENTITY_ID_1;
import static com.github.nagyesta.lowkeyvault.service.certificate.CertificateLifetimeActionActivity.AUTO_RENEW;
import static com.github.nagyesta.lowkeyvault.service.certificate.CertificateLifetimeActionActivity.EMAIL_CONTACTS;
import static com.github.nagyesta.lowkeyvault.service.certificate.CertificateLifetimeActionTriggerType.DAYS_BEFORE_EXPIRY;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class CertificateLifetimeActionPolicyTest {

    private static final int DAYS_120_MONTHS = 3600;
    private static final int DAYS_90_MONTHS = 3300;
    private static final int DAYS_10_MONTHS = 300;
    private static final int DAYS_2_MONTHS = 60;
    private static final OffsetDateTime DATE_100_MONTHS_AGO = NOW.minusDays(DAYS_120_MONTHS);
    private static final OffsetDateTime DATE_88_MONTHS_AGO = NOW.minusDays(3240);

    public static Stream<Arguments> nullProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(null, null))
                .add(Arguments.of(UNVERSIONED_CERT_ENTITY_ID_1, null))
                .add(Arguments.of(null, Map.of()))
                .build();
    }

    @ParameterizedTest
    @MethodSource("nullProvider")
    void testConstructorShouldThrowExceptionWhenCalledWithNull(
            final CertificateEntityId id,
            final Map<CertificateLifetimeActionActivity, CertificateLifetimeActionTrigger> map) {
        //given

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> new CertificateLifetimeActionPolicy(id, map));

        //then + exception
    }

    @Test
    void testGetLifetimeActionsShouldReturnTheMapSetPreviouslyWhenCalled() {
        //given
        final Map<CertificateLifetimeActionActivity, CertificateLifetimeActionTrigger> expected =
                Map.of(EMAIL_CONTACTS, new CertificateLifetimeActionTrigger(DAYS_BEFORE_EXPIRY, 10));
        final CertificateLifetimeActionPolicy underTest = new CertificateLifetimeActionPolicy(UNVERSIONED_CERT_ENTITY_ID_1, expected);

        //when
        final Map<CertificateLifetimeActionActivity, CertificateLifetimeActionTrigger> actual = underTest.getLifetimeActions();

        //then
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void testSetLifetimeActionsShouldOverwriteTheMapWhenCalledWithValidData() {
        //given
        final Map<CertificateLifetimeActionActivity, CertificateLifetimeActionTrigger> expected =
                Map.of(EMAIL_CONTACTS, new CertificateLifetimeActionTrigger(DAYS_BEFORE_EXPIRY, 10));
        final CertificateLifetimeActionPolicy underTest = new CertificateLifetimeActionPolicy(UNVERSIONED_CERT_ENTITY_ID_1, Map.of());

        //when
        underTest.setLifetimeActions(expected);

        //then
        final Map<CertificateLifetimeActionActivity, CertificateLifetimeActionTrigger> actual = underTest.getLifetimeActions();
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void testIsAutoRenewShouldReturnFalseWhenCalledWithOnlyEmailContactsSet() {
        //given
        final Map<CertificateLifetimeActionActivity, CertificateLifetimeActionTrigger> expected =
                Map.of(EMAIL_CONTACTS, new CertificateLifetimeActionTrigger(DAYS_BEFORE_EXPIRY, 10));
        final CertificateLifetimeActionPolicy underTest = new CertificateLifetimeActionPolicy(UNVERSIONED_CERT_ENTITY_ID_1, expected);

        //when
        final boolean actual = underTest.isAutoRenew();

        //then
        Assertions.assertFalse(actual);
    }

    @Test
    void testIsAutoRenewShouldReturnTrueWhenCalledWithBothTypesInMap() {
        //given
        final CertificateLifetimeActionTrigger trigger = new CertificateLifetimeActionTrigger(DAYS_BEFORE_EXPIRY, 10);
        final Map<CertificateLifetimeActionActivity, CertificateLifetimeActionTrigger> expected = Map
                .of(EMAIL_CONTACTS, trigger, AUTO_RENEW, trigger);
        final CertificateLifetimeActionPolicy underTest = new CertificateLifetimeActionPolicy(UNVERSIONED_CERT_ENTITY_ID_1, expected);

        //when
        final boolean actual = underTest.isAutoRenew();

        //then
        Assertions.assertTrue(actual);
    }

    @Test
    void testValidateShouldCallValidateOfAllTriggersWhenCalled() {
        //given
        final CertificateLifetimeActionTrigger emailTrigger = mock(CertificateLifetimeActionTrigger.class);
        final CertificateLifetimeActionTrigger renewTrigger = mock(CertificateLifetimeActionTrigger.class);
        final Map<CertificateLifetimeActionActivity, CertificateLifetimeActionTrigger> expected = Map
                .of(EMAIL_CONTACTS, emailTrigger, AUTO_RENEW, renewTrigger);
        final CertificateLifetimeActionPolicy underTest = new CertificateLifetimeActionPolicy(UNVERSIONED_CERT_ENTITY_ID_1, expected);

        //when
        underTest.validate(1);

        //then
        verify(emailTrigger).validate(eq(1));
        verify(renewTrigger).validate(eq(1));
    }

    @Test
    void testMissedRenewalDaysShouldReturnMissedRenewalDatesWhenCalledOnPolicyWithMissedRenewals() {
        //given
        final CertificateLifetimeActionTrigger emailTrigger = mock(CertificateLifetimeActionTrigger.class);
        final CertificateLifetimeActionTrigger renewTrigger = new CertificateLifetimeActionTrigger(DAYS_BEFORE_EXPIRY, DAYS_2_MONTHS);
        final Map<CertificateLifetimeActionActivity, CertificateLifetimeActionTrigger> actions = Map
                .of(EMAIL_CONTACTS, emailTrigger, AUTO_RENEW, renewTrigger);
        final CertificateLifetimeActionPolicy underTest = new CertificateLifetimeActionPolicy(UNVERSIONED_CERT_ENTITY_ID_1, actions);
        underTest.setCreatedOn(DATE_100_MONTHS_AGO);

        //when
        final List<OffsetDateTime> actual = underTest.missedRenewalDays(DATE_100_MONTHS_AGO, DATE_88_MONTHS_AGO);

        //then
        final List<OffsetDateTime> expected = Stream.iterate(DAYS_90_MONTHS, a -> a - DAYS_10_MONTHS)
                .limit(DAYS_120_MONTHS / DAYS_10_MONTHS)
                .map(NOW::minusDays)
                .collect(Collectors.toList());
        Assertions.assertIterableEquals(expected, actual);
        verifyNoInteractions(emailTrigger);
    }
}
