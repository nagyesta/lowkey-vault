package com.github.nagyesta.lowkeyvault.service.certificate.impl;

import com.github.nagyesta.lowkeyvault.service.certificate.CertificateLifetimeActionTrigger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.stream.Stream;

import static com.github.nagyesta.lowkeyvault.TestConstants.NOW;
import static com.github.nagyesta.lowkeyvault.TestConstantsCertificates.UNVERSIONED_CERT_ENTITY_ID_1;
import static com.github.nagyesta.lowkeyvault.service.certificate.CertificateLifetimeActionActivity.AUTO_RENEW;
import static com.github.nagyesta.lowkeyvault.service.certificate.CertificateLifetimeActionActivity.EMAIL_CONTACTS;
import static com.github.nagyesta.lowkeyvault.service.certificate.CertificateLifetimeActionTriggerType.DAYS_BEFORE_EXPIRY;
import static com.github.nagyesta.lowkeyvault.service.certificate.impl.CertificateCreationInput.DEFAULT_VALIDITY_MONTHS;
import static java.time.temporal.ChronoUnit.DAYS;

class CertificateLifetimeActionPolicyTest {
    private static final int DAYS_27 = 27;
    private static final int DAYS_60 = 60;
    private static final int MONTHS_100 = 100;
    private static final OffsetDateTime DATE_100_MONTHS_AGO = NOW.minusMonths(MONTHS_100);
    private static final int VALIDITY_MONTHS = 12;

    @Test
    void testGetLifetimeActionsShouldReturnTheMapSetPreviouslyWhenCalled() {
        //given
        final var trigger = new CertificateLifetimeActionTrigger(DAYS_BEFORE_EXPIRY, 10);
        final var expected = Map.of(EMAIL_CONTACTS, trigger);
        final var underTest = new CertificateLifetimeActionPolicy(UNVERSIONED_CERT_ENTITY_ID_1, expected);

        //when
        final var actual = underTest.getLifetimeActions();

        //then
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void testSetLifetimeActionsShouldOverwriteTheMapWhenCalledWithValidData() {
        //given
        final var trigger = new CertificateLifetimeActionTrigger(DAYS_BEFORE_EXPIRY, 10);
        final var expected = Map.of(EMAIL_CONTACTS, trigger);
        final var underTest = new CertificateLifetimeActionPolicy(UNVERSIONED_CERT_ENTITY_ID_1, Map.of());

        //when
        underTest.setLifetimeActions(expected);

        //then
        final var actual = underTest.getLifetimeActions();
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void testIsAutoRenewShouldReturnFalseWhenCalledWithOnlyEmailContactsSet() {
        //given
        final var trigger = new CertificateLifetimeActionTrigger(DAYS_BEFORE_EXPIRY, 10);
        final var expected = Map.of(EMAIL_CONTACTS, trigger);
        final var underTest = new CertificateLifetimeActionPolicy(UNVERSIONED_CERT_ENTITY_ID_1, expected);

        //when
        final var actual = underTest.isAutoRenew();

        //then
        Assertions.assertFalse(actual);
    }

    @Test
    void testIsAutoRenewShouldReturnTrueWhenCalledWithBothTypesInMap() {
        //given
        final var trigger = new CertificateLifetimeActionTrigger(DAYS_BEFORE_EXPIRY, 10);
        final var expected = Map.of(EMAIL_CONTACTS, trigger, AUTO_RENEW, trigger);
        final var underTest = new CertificateLifetimeActionPolicy(UNVERSIONED_CERT_ENTITY_ID_1, expected);

        //when
        final var actual = underTest.isAutoRenew();

        //then
        Assertions.assertTrue(actual);
    }

    @Test
    void testValidateShouldCallValidateOfAllTriggersWhenCalled() {
        //given
        final var emailTrigger = new CertificateLifetimeActionTrigger(DAYS_BEFORE_EXPIRY, DAYS_27);
        final var renewTrigger = new CertificateLifetimeActionTrigger(DAYS_BEFORE_EXPIRY, DAYS_60);
        final var expected = Map.of(EMAIL_CONTACTS, emailTrigger, AUTO_RENEW, renewTrigger);
        final var underTest = new CertificateLifetimeActionPolicy(UNVERSIONED_CERT_ENTITY_ID_1, expected);

        //when
        underTest.validate(VALIDITY_MONTHS);

        //then no exception
    }

    @Test
    void testValidateShouldThrowExceptionWhenCalledWithInvalidTriggers() {
        //given
        final var emailTrigger = new CertificateLifetimeActionTrigger(DAYS_BEFORE_EXPIRY, DAYS_27);
        final var renewTrigger = new CertificateLifetimeActionTrigger(DAYS_BEFORE_EXPIRY, DAYS_60);
        final var expected = Map.of(EMAIL_CONTACTS, emailTrigger, AUTO_RENEW, renewTrigger);
        final var underTest = new CertificateLifetimeActionPolicy(UNVERSIONED_CERT_ENTITY_ID_1, expected);

        //when
        Assertions.assertThrows(IllegalStateException.class, () -> underTest.validate(1));

        //then exception
    }

    @Test
    void testMissedRenewalDaysShouldReturnMissedRenewalDatesWhenCalledOnPolicyWithMissedRenewals() {
        //given
        final var emailTrigger = new CertificateLifetimeActionTrigger(DAYS_BEFORE_EXPIRY, DAYS_60);
        final var renewTrigger = new CertificateLifetimeActionTrigger(DAYS_BEFORE_EXPIRY, DAYS_60);
        final var actions = Map.of(EMAIL_CONTACTS, emailTrigger, AUTO_RENEW, renewTrigger);
        final var underTest = new CertificateLifetimeActionPolicy(UNVERSIONED_CERT_ENTITY_ID_1, actions);
        underTest.setCreated(DATE_100_MONTHS_AGO);

        //when
        final var actual = underTest.missedRenewalDays(DATE_100_MONTHS_AGO, s -> s.plusMonths(DEFAULT_VALIDITY_MONTHS));
        final var firstRenewal = DAYS.between(nextRenewal(DATE_100_MONTHS_AGO), NOW);

        //then
        final var expected = Stream.iterate(firstRenewal, a -> DAYS.between(nextRenewal(NOW.minusDays(a)), NOW))
                .limit(MONTHS_100 / VALIDITY_MONTHS + 1)
                .map(NOW::minusDays)
                .toList();
        Assertions.assertIterableEquals(expected, actual);
    }

    private static OffsetDateTime nextRenewal(final OffsetDateTime start) {
        return start.plusMonths(VALIDITY_MONTHS).minusDays(DAYS_60);
    }
}
