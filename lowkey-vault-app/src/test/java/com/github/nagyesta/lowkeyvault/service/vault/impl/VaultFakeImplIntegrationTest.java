package com.github.nagyesta.lowkeyvault.service.vault.impl;

import com.github.nagyesta.lowkeyvault.model.v7_2.common.constants.RecoveryLevel;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyCurveName;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyType;
import com.github.nagyesta.lowkeyvault.service.certificate.CertificateLifetimeActionTrigger;
import com.github.nagyesta.lowkeyvault.service.certificate.CertificateVaultFake;
import com.github.nagyesta.lowkeyvault.service.certificate.ReadOnlyKeyVaultCertificateEntity;
import com.github.nagyesta.lowkeyvault.service.certificate.id.VersionedCertificateEntityId;
import com.github.nagyesta.lowkeyvault.service.certificate.impl.*;
import com.github.nagyesta.lowkeyvault.service.secret.impl.KeyVaultSecretEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.github.nagyesta.lowkeyvault.TestConstantsCertificates.CERT_NAME_1;
import static com.github.nagyesta.lowkeyvault.TestConstantsUri.HTTPS_LOCALHOST;
import static com.github.nagyesta.lowkeyvault.service.certificate.CertificateLifetimeActionActivity.AUTO_RENEW;
import static com.github.nagyesta.lowkeyvault.service.certificate.CertificateLifetimeActionTriggerType.DAYS_BEFORE_EXPIRY;
import static com.github.nagyesta.lowkeyvault.service.certificate.impl.CertificateCreationInput.DEFAULT_VALIDITY_MONTHS;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.MONTHS;

class VaultFakeImplIntegrationTest {

    private static final int INT_800 = 800;
    private static final int SECONDS_IN_1_DAY = 24 * 3600;
    private static final int SECONDS_IN_800_DAYS = INT_800 * SECONDS_IN_1_DAY;
    public static final int EXPECTED_VERSIONS_AFTER_RENEWAL = 3;

    @Test
    void testTimeShiftShouldCreateNewVersionsWhenAutoRotateIsTriggeredWithActiveCertificates() {
        //given
        final VaultFakeImpl underTest = new VaultFakeImpl(HTTPS_LOCALHOST);
        final CertificateVaultFake certificateVaultFake = underTest.certificateVaultFake();
        final OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        final OffsetDateTime approxNow = now.plusMinutes(1);
        final VersionedCertificateEntityId originalCertId = certificateVaultFake
                .createCertificateVersion(CERT_NAME_1, CertificateCreationInput.builder()
                        .contentType(CertContentType.PEM)
                        .name(CERT_NAME_1)
                        .keyType(KeyType.EC)
                        .validityStart(approxNow)
                        .validityMonths(DEFAULT_VALIDITY_MONTHS)
                        .keyCurveName(KeyCurveName.P_521)
                        .subject("CN=localhost")
                        .build());
        final int triggerThresholdDays = 1;
        certificateVaultFake.setLifetimeActionPolicy(new CertificateLifetimeActionPolicy(
                originalCertId, Map.of(AUTO_RENEW, new CertificateLifetimeActionTrigger(DAYS_BEFORE_EXPIRY, triggerThresholdDays))
        ));

        //when
        underTest.timeShift(SECONDS_IN_800_DAYS, true);

        //then
        final Deque<String> versions = underTest.certificateVaultFake().getEntities().getVersions(originalCertId);
        final List<ReadOnlyKeyVaultCertificateEntity> entities = versions.stream()
                .map(v -> new VersionedCertificateEntityId(originalCertId.vault(), originalCertId.id(), v))
                .map(certificateVaultFake.getEntities()::getReadOnlyEntity)
                .collect(Collectors.toList());
        Assertions.assertEquals(EXPECTED_VERSIONS_AFTER_RENEWAL, entities.size());
        final ReadOnlyKeyVaultCertificateEntity recreatedOriginal = entities.get(0);
        final ReadOnlyKeyVaultCertificateEntity firstRenewal = entities.get(1);
        final ReadOnlyKeyVaultCertificateEntity secondRenewal = entities.get(2);
        assertTimestampsAreAdjustedAsExpected(approxNow, recreatedOriginal, INT_800);
        final OffsetDateTime firstRenewalDay = recreatedOriginal.getExpiry().map(v -> v.minusDays(triggerThresholdDays)).orElseThrow();
        assertTimestampsAreAdjustedAsExpected(approxNow, firstRenewal, DAYS.between(firstRenewalDay, approxNow));
        final OffsetDateTime secondRenewalDay = firstRenewal.getExpiry().map(v -> v.minusDays(triggerThresholdDays)).orElseThrow();
        assertTimestampsAreAdjustedAsExpected(approxNow, secondRenewal, DAYS.between(secondRenewalDay, approxNow));
        Assertions.assertNotEquals(recreatedOriginal.getKid(), firstRenewal.getKid());
        Assertions.assertNotEquals(recreatedOriginal.getKid(), secondRenewal.getKid());
    }

    @Test
    void testTimeShiftShouldCreateNewVersionsWhenAutoRotateIsTriggeredWithDifferentContentTypeInIssuancePolicy() {
        //given
        final VaultFakeImpl underTest = new VaultFakeImpl(HTTPS_LOCALHOST);
        final CertificateVaultFake certificateVaultFake = underTest.certificateVaultFake();
        final OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        final OffsetDateTime approxNow = now.plusMinutes(1);
        final VersionedCertificateEntityId originalCertId = certificateVaultFake
                .createCertificateVersion(CERT_NAME_1, CertificateCreationInput.builder()
                        .contentType(CertContentType.PKCS12)
                        .name(CERT_NAME_1)
                        .keyType(KeyType.EC)
                        .validityStart(approxNow)
                        .validityMonths(DEFAULT_VALIDITY_MONTHS)
                        .keyCurveName(KeyCurveName.P_521)
                        .subject("CN=localhost")
                        .build());
        final int triggerThresholdDays = 1;
        certificateVaultFake.setLifetimeActionPolicy(new CertificateLifetimeActionPolicy(
                originalCertId, Map.of(AUTO_RENEW, new CertificateLifetimeActionTrigger(DAYS_BEFORE_EXPIRY, triggerThresholdDays))
        ));
        final CertificatePolicy issuancePolicy = (CertificatePolicy) certificateVaultFake.getEntities()
                .getEntity(originalCertId, KeyVaultCertificateEntity.class)
                .getIssuancePolicy();
        issuancePolicy.setContentType(CertContentType.PEM);

        //when
        underTest.timeShift(SECONDS_IN_800_DAYS, true);

        //then
        final Deque<String> versions = underTest.certificateVaultFake().getEntities().getVersions(originalCertId);
        final List<ReadOnlyKeyVaultCertificateEntity> entities = versions.stream()
                .map(v -> new VersionedCertificateEntityId(originalCertId.vault(), originalCertId.id(), v))
                .map(certificateVaultFake.getEntities()::getReadOnlyEntity)
                .collect(Collectors.toList());
        Assertions.assertEquals(EXPECTED_VERSIONS_AFTER_RENEWAL, entities.size());
        assertRenewalUsedPem(underTest, entities.get(1));
        assertRenewalUsedPem(underTest, entities.get(2));
    }

    @Test
    void testTimeShiftShouldCreateNewVersionsUsingSameKeyWhenAutoRotateIsTriggeredWithActiveCertificatesAllowingKeyReuse() {
        //given
        final VaultFakeImpl underTest = new VaultFakeImpl(HTTPS_LOCALHOST);
        final CertificateVaultFake certificateVaultFake = underTest.certificateVaultFake();
        final OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        final OffsetDateTime approxNow = now.plusMinutes(1);
        final VersionedCertificateEntityId originalCertId = certificateVaultFake
                .createCertificateVersion(CERT_NAME_1, CertificateCreationInput.builder()
                        .contentType(CertContentType.PEM)
                        .name(CERT_NAME_1)
                        .keyType(KeyType.EC)
                        .validityStart(approxNow)
                        .validityMonths(DEFAULT_VALIDITY_MONTHS)
                        .keyCurveName(KeyCurveName.P_521)
                        .subject("CN=localhost")
                        .reuseKeyOnRenewal(true)
                        .build());
        final int triggerThresholdDays = 1;
        certificateVaultFake.setLifetimeActionPolicy(new CertificateLifetimeActionPolicy(
                originalCertId, Map.of(AUTO_RENEW, new CertificateLifetimeActionTrigger(DAYS_BEFORE_EXPIRY, triggerThresholdDays))
        ));

        //when
        underTest.timeShift(SECONDS_IN_800_DAYS, true);

        //then
        final Deque<String> versions = underTest.certificateVaultFake().getEntities().getVersions(originalCertId);
        final List<ReadOnlyKeyVaultCertificateEntity> entities = versions.stream()
                .map(v -> new VersionedCertificateEntityId(originalCertId.vault(), originalCertId.id(), v))
                .map(certificateVaultFake.getEntities()::getReadOnlyEntity)
                .collect(Collectors.toList());
        Assertions.assertEquals(EXPECTED_VERSIONS_AFTER_RENEWAL, entities.size());
        final ReadOnlyKeyVaultCertificateEntity recreatedOriginal = entities.get(0);
        final ReadOnlyKeyVaultCertificateEntity firstRenewal = entities.get(1);
        final ReadOnlyKeyVaultCertificateEntity secondRenewal = entities.get(2);
        assertTimestampsAreAdjustedAsExpected(approxNow, recreatedOriginal, INT_800);
        final OffsetDateTime firstRenewalDay = recreatedOriginal.getExpiry().map(v -> v.minusDays(triggerThresholdDays)).orElseThrow();
        assertTimestampsAreAdjustedAsExpected(approxNow, firstRenewal, DAYS.between(firstRenewalDay, approxNow));
        final OffsetDateTime secondRenewalDay = firstRenewal.getExpiry().map(v -> v.minusDays(triggerThresholdDays)).orElseThrow();
        assertTimestampsAreAdjustedAsExpected(approxNow, secondRenewal, DAYS.between(secondRenewalDay, approxNow));
        Assertions.assertEquals(recreatedOriginal.getKid(), firstRenewal.getKid());
        Assertions.assertEquals(recreatedOriginal.getKid(), secondRenewal.getKid());
    }

    @Test
    void testTimeShiftShouldNotCreateNewVersionsWhenAutoRotateIsTriggeredWithDeletedCertificatesAsTheyArePurged() {
        //given
        final VaultFakeImpl underTest = new VaultFakeImpl(HTTPS_LOCALHOST,
                RecoveryLevel.RECOVERABLE_AND_PURGEABLE, RecoveryLevel.MAX_RECOVERABLE_DAYS_INCLUSIVE);
        final CertificateVaultFake certificateVaultFake = underTest.certificateVaultFake();
        final OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        final OffsetDateTime approxNow = now.plusMinutes(1);
        final VersionedCertificateEntityId originalCertId = certificateVaultFake
                .createCertificateVersion(CERT_NAME_1, CertificateCreationInput.builder()
                        .contentType(CertContentType.PEM)
                        .name(CERT_NAME_1)
                        .keyType(KeyType.EC)
                        .validityStart(approxNow)
                        .validityMonths(DEFAULT_VALIDITY_MONTHS)
                        .keyCurveName(KeyCurveName.P_521)
                        .subject("CN=localhost")
                        .build());
        final int triggerThresholdDays = 1;
        certificateVaultFake.setLifetimeActionPolicy(new CertificateLifetimeActionPolicy(
                originalCertId, Map.of(AUTO_RENEW, new CertificateLifetimeActionTrigger(DAYS_BEFORE_EXPIRY, triggerThresholdDays))
        ));
        certificateVaultFake.delete(originalCertId);

        //when
        underTest.timeShift(SECONDS_IN_800_DAYS, true);

        //then
        final boolean exists = underTest.certificateVaultFake().getDeletedEntities().containsName(originalCertId.id());
        Assertions.assertFalse(exists);
    }

    @Test
    void testTimeShiftShouldNotCreateNewVersionsWhenAutoRotateIsTriggeredWithDeletedCertificatesEvenIfNotPurged() {
        //given
        final VaultFakeImpl underTest = new VaultFakeImpl(HTTPS_LOCALHOST,
                RecoveryLevel.RECOVERABLE_AND_PURGEABLE, RecoveryLevel.MAX_RECOVERABLE_DAYS_INCLUSIVE);
        final CertificateVaultFake certificateVaultFake = underTest.certificateVaultFake();
        final OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        final OffsetDateTime approxNow = now.plusMinutes(1);
        final VersionedCertificateEntityId originalCertId = certificateVaultFake
                .createCertificateVersion(CERT_NAME_1, CertificateCreationInput.builder()
                        .contentType(CertContentType.PEM)
                        .name(CERT_NAME_1)
                        .keyType(KeyType.EC)
                        .validityStart(approxNow)
                        .validityMonths(DEFAULT_VALIDITY_MONTHS)
                        .keyCurveName(KeyCurveName.P_521)
                        .subject("CN=localhost")
                        .build());
        final int triggerThresholdDays = 1;
        certificateVaultFake.setLifetimeActionPolicy(new CertificateLifetimeActionPolicy(
                originalCertId, Map.of(AUTO_RENEW, new CertificateLifetimeActionTrigger(DAYS_BEFORE_EXPIRY, triggerThresholdDays))
        ));
        certificateVaultFake.delete(originalCertId);

        //when
        underTest.timeShift(SECONDS_IN_1_DAY, true);

        //then
        final Deque<String> versions = underTest.certificateVaultFake().getDeletedEntities().getVersions(originalCertId);
        Assertions.assertIterableEquals(Set.of(originalCertId.version()), versions);
    }

    private static void assertRenewalUsedPem(final VaultFakeImpl underTest, final ReadOnlyKeyVaultCertificateEntity certificateEntity) {
        Assertions.assertEquals(CertContentType.PEM, certificateEntity.getOriginalCertificatePolicy().getContentType());
        final KeyVaultSecretEntity secretEntity = underTest.secretVaultFake().getEntities()
                .getEntity(certificateEntity.getSid(), KeyVaultSecretEntity.class);
        Assertions.assertEquals(CertContentType.PEM.getMimeType(), secretEntity.getContentType());
        Assertions.assertNotNull(CertContentType.PEM.getCertificateChain(secretEntity.getValue(), ""));
    }

    private static void assertTimestampsAreAdjustedAsExpected(
            final OffsetDateTime approxNow, final ReadOnlyKeyVaultCertificateEntity recreatedOriginal, final long expectedCreationDaysAgo) {
        Assertions.assertEquals(expectedCreationDaysAgo, DAYS.between(recreatedOriginal.getCreated(), approxNow));
        Assertions.assertEquals(expectedCreationDaysAgo, DAYS.between(recreatedOriginal.getUpdated(), approxNow));
        Assertions.assertEquals(expectedCreationDaysAgo, DAYS.between(recreatedOriginal.getNotBefore().orElseThrow(), approxNow));
        Assertions.assertEquals(DEFAULT_VALIDITY_MONTHS, MONTHS
                .between(recreatedOriginal.getNotBefore().orElseThrow(), recreatedOriginal.getExpiry().orElseThrow()));
    }
}
