package com.github.nagyesta.lowkeyvault.controller.v7_6;

import com.github.nagyesta.abortmission.booster.jupiter.annotation.LaunchAbortArmed;
import com.github.nagyesta.lowkeyvault.ResourceUtils;
import com.github.nagyesta.lowkeyvault.TestConstants;
import com.github.nagyesta.lowkeyvault.controller.v7_3.BaseCertificateControllerIntegrationTest;
import com.github.nagyesta.lowkeyvault.model.common.DeletedModel;
import com.github.nagyesta.lowkeyvault.model.v7_3.certificate.*;
import com.github.nagyesta.lowkeyvault.service.certificate.CertificateLifetimeActionActivity;
import com.github.nagyesta.lowkeyvault.service.certificate.id.CertificateEntityId;
import com.github.nagyesta.lowkeyvault.service.certificate.id.VersionedCertificateEntityId;
import com.github.nagyesta.lowkeyvault.service.certificate.impl.CertContentType;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.github.nagyesta.lowkeyvault.TestConstantsCertificates.*;
import static com.github.nagyesta.lowkeyvault.TestConstantsUri.getRandomVaultUri;
import static com.github.nagyesta.lowkeyvault.TestConstantsUri.getRandomVaultUriAsString;
import static com.github.nagyesta.lowkeyvault.model.common.ApiConstants.V_7_6;
import static com.github.nagyesta.lowkeyvault.service.certificate.CertificateLifetimeActionActivity.AUTO_RENEW;
import static com.github.nagyesta.lowkeyvault.service.certificate.CertificateLifetimeActionActivity.EMAIL_CONTACTS;
import static com.github.nagyesta.lowkeyvault.service.certificate.impl.CertificateCreationInput.DEFAULT_EC_KEY_USAGES;
import static com.github.nagyesta.lowkeyvault.service.certificate.impl.CertificateCreationInput.DEFAULT_EXT_KEY_USAGES;
import static org.springframework.http.HttpStatus.*;

@LaunchAbortArmed
@SpringBootTest
class CertificateControllerIntegrationTest extends BaseCertificateControllerIntegrationTest {

    private static final int A_HUNDRED_YEARS = 36500;
    private static final int ONE_HUNDRED = 100;
    private static final URI VAULT_URI_1 = getRandomVaultUri();
    private static final URI VAULT_URI_2 = getRandomVaultUri();
    @Autowired
    @Qualifier("certificateControllerV76")
    private CertificateController underTest;

    @BeforeEach
    void setUp() {
        prepareVaultIfNotExists(VAULT_URI_1);
        prepareVaultIfNotExists(VAULT_URI_2);
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    public static Stream<Arguments> certificateListProvider() {
        final var uri1 = getRandomVaultUriAsString();
        final var uri2 = getRandomVaultUriAsString();
        final var uri3 = getRandomVaultUriAsString();
        return Stream.<Arguments>builder()
                .add(Arguments.of(uri1, 5, 0,
                        uri1 + "/certificates?api-version=7.6&$skiptoken=5&maxresults=5&includePending=true"))
                .add(Arguments.of(uri2, 3, 1,
                        uri2 + "/certificates?api-version=7.6&$skiptoken=4&maxresults=3&includePending=true"))
                .add(Arguments.of(uri3, 3, 8,
                        null))
                .build();
    }

    @Test
    void testCreateShouldReturnPendingCertificateWhenCalled() {
        //given
        final var request = getCreateCertificateRequest();

        //when
        final var actual = underTest
                .create("create-" + CERT_NAME_1, VAULT_URI_1, request);

        //then
        Assertions.assertEquals(ACCEPTED, actual.getStatusCode());
        final var body = actual.getBody();
        assertPendingCreateResponseIsAsExpected(body);
    }

    @Test
    void testCreateShouldThrowExceptionWhenCalledWithInvalidLifetimeActionsWithZeroPercentageTrigger() {
        //given
        final var request = getCreateCertificateRequest();
        final var actions = List.of(
                lifetimeActivity(EMAIL_CONTACTS, lifeTimePercentageTrigger(0)));
        request.getPolicy().setLifetimeActions(actions);

        //when
        Assertions.assertThrows(ConstraintViolationException.class, () -> underTest
                .create("create-invalid-" + CERT_NAME_1, VAULT_URI_1, request));

        //then + exception
    }

    @Test
    void testCreateShouldThrowExceptionWhenCalledWithInvalidLifetimeActionsWithTriggerUsingTooManyDaysBeforeExpiry() {
        //given
        final var request = getCreateCertificateRequest();
        final var actions = List.of(
                lifetimeActivity(EMAIL_CONTACTS, daysBeforeExpiryTrigger(A_HUNDRED_YEARS)));
        request.getPolicy().setLifetimeActions(actions);

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest
                .create("create-invalid-" + CERT_NAME_1, VAULT_URI_1, request));

        //then + exception
    }

    @Test
    void testCreateShouldThrowExceptionWhenCalledWithInvalidLifetimeActionsUsingTwoTriggers() {
        //given
        final var request = getCreateCertificateRequest();
        final var actions = List.of(
                lifetimeActivity(AUTO_RENEW, daysBeforeExpiryTrigger(1)),
                lifetimeActivity(EMAIL_CONTACTS, daysBeforeExpiryTrigger(1))
        );
        request.getPolicy().setLifetimeActions(actions);

        //when
        Assertions.assertThrows(ConstraintViolationException.class, () -> underTest
                .create("create-invalid-" + CERT_NAME_1, VAULT_URI_1, request));

        //then + exception
    }

    @Test
    void testCreateShouldReturnPendingCertificateWhenCalledWithValidLifetimeActions() {
        //given
        final var request = getCreateCertificateRequest();
        request.getPolicy().setLifetimeActions(List.of(lifetimeActivity(EMAIL_CONTACTS, daysBeforeExpiryTrigger(1))));

        //when
        final var actual = underTest
                .create("create-lifetime-" + CERT_NAME_1, VAULT_URI_1, request);

        //then
        Assertions.assertEquals(ACCEPTED, actual.getStatusCode());
        final var body = actual.getBody();
        assertPendingCreateResponseIsAsExpected(body);
    }

    @Test
    void testGetShouldReturnModelWhenCalledWithValidData() {
        //given
        final var request = getCreateCertificateRequest();
        underTest.create(CERT_NAME_2, VAULT_URI_1, request);
        final var versions = findByUri(VAULT_URI_1)
                .certificateVaultFake()
                .getEntities()
                .getVersions(new CertificateEntityId(VAULT_URI_1, CERT_NAME_2));

        //when
        final var actual = underTest.get(CERT_NAME_2, VAULT_URI_1);

        //then
        Assertions.assertEquals(OK, actual.getStatusCode());
        final var body = actual.getBody();
        Assertions.assertNotNull(body);
        Assertions.assertNotNull(body.getCertificate());
        Assertions.assertNotNull(body.getThumbprint());
        Assertions.assertEquals(Collections.emptyMap(), body.getTags());
        final var id = new VersionedCertificateEntityId(VAULT_URI_1, CERT_NAME_2, versions.getFirst())
                .asUri(VAULT_URI_1);
        Assertions.assertEquals(id.toString(), body.getId());
    }

    @Test
    void testGetWithVersionShouldReturnModelWhenCalledWithValidData() {
        //given
        final var request = getCreateCertificateRequest();
        underTest.create(CERT_NAME_3, VAULT_URI_1, request);
        final var versions = findByUri(VAULT_URI_1)
                .certificateVaultFake()
                .getEntities()
                .getVersions(new CertificateEntityId(VAULT_URI_1, CERT_NAME_3));

        //when
        final var actual = underTest
                .getWithVersion(CERT_NAME_3, versions.getFirst(), VAULT_URI_1);

        //then
        Assertions.assertEquals(OK, actual.getStatusCode());
        final var body = actual.getBody();
        Assertions.assertNotNull(body);
        Assertions.assertNotNull(body.getCertificate());
        Assertions.assertNotNull(body.getThumbprint());
        Assertions.assertEquals(Collections.emptyMap(), body.getTags());
        final var id = new VersionedCertificateEntityId(VAULT_URI_1, CERT_NAME_3, versions.getFirst())
                .asUri(VAULT_URI_1);
        Assertions.assertEquals(id.toString(), body.getId());
    }

    @Test
    void testApiVersionShouldReturnV76WhenCalled() {
        //given

        //when
        final var actual = underTest.apiVersion();

        //then
        Assertions.assertEquals(V_7_6, actual);
    }

    @Test
    void testImportCertificateShouldReturnModelWhenCalledWithValidPemData() {
        //given
        final var request = getCreateImportRequest("/cert/ec.pem", CertContentType.PEM);
        final var name = CERT_NAME_3 + "-import-pem";

        //when
        final var actual = underTest
                .importCertificate(name, VAULT_URI_1, request);

        //then
        Assertions.assertEquals(OK, actual.getStatusCode());
        final var body = actual.getBody();
        Assertions.assertNotNull(body);
        Assertions.assertNotNull(body.getCertificate());
        Assertions.assertNotNull(body.getThumbprint());
        Assertions.assertEquals(Collections.emptyMap(), body.getTags());
        Assertions.assertTrue(body.getId().startsWith(VAULT_URI_1.toString()));
        Assertions.assertTrue(body.getId().contains(name));
    }

    @Test
    void testImportCertificateShouldReturnModelWhenCalledWithValidPkcs12Data() {
        //given
        final var request = getCreateImportRequest("/cert/ec.p12", CertContentType.PKCS12);
        final var name = CERT_NAME_3 + "-import-pkcs";

        //when
        final var actual = underTest
                .importCertificate(name, VAULT_URI_1, request);

        //then
        Assertions.assertEquals(OK, actual.getStatusCode());
        final var body = actual.getBody();
        Assertions.assertNotNull(body);
        Assertions.assertNotNull(body.getCertificate());
        Assertions.assertNotNull(body.getThumbprint());
        Assertions.assertEquals(Collections.emptyMap(), body.getTags());
        Assertions.assertTrue(body.getId().startsWith(VAULT_URI_1.toString()));
        Assertions.assertTrue(body.getId().contains(name));
    }

    @Test
    void testImportCertificateShouldReturnModelWhenCalledWithValidPemDataAndNoType() {
        //given
        final var request = getCreateImportRequest("/cert/ec.pem", null);
        final var name = CERT_NAME_3 + "-import-pem-auto";

        //when
        final var actual = underTest
                .importCertificate(name, VAULT_URI_1, request);

        //then
        Assertions.assertEquals(OK, actual.getStatusCode());
        final var body = actual.getBody();
        Assertions.assertNotNull(body);
        Assertions.assertNotNull(body.getCertificate());
        Assertions.assertNotNull(body.getThumbprint());
        Assertions.assertEquals(Collections.emptyMap(), body.getTags());
        Assertions.assertTrue(body.getId().startsWith(VAULT_URI_1.toString()));
        Assertions.assertTrue(body.getId().contains(name));
    }

    @Test
    void testImportCertificateShouldReturnModelWhenCalledWithValidPkcs12DataAndNoType() {
        //given
        final var request = getCreateImportRequest("/cert/ec.p12", null);
        final var name = CERT_NAME_3 + "-import-pkcs-auto";

        //when
        final var actual = underTest
                .importCertificate(name, VAULT_URI_1, request);

        //then
        Assertions.assertEquals(OK, actual.getStatusCode());
        final var body = actual.getBody();
        Assertions.assertNotNull(body);
        Assertions.assertNotNull(body.getCertificate());
        Assertions.assertNotNull(body.getThumbprint());
        Assertions.assertEquals(Collections.emptyMap(), body.getTags());
        Assertions.assertTrue(body.getId().startsWith(VAULT_URI_1.toString()));
        Assertions.assertTrue(body.getId().contains(name));
    }

    @Test
    void testImportCertificateShouldReturnModelWhenCalledWithValidPkcs12DataAndLifetimeAction() {
        //given
        final var request = getCreateImportRequest("/cert/ec.p12", null);
        final var name = CERT_NAME_3 + "-import-pkcs-lifetime";
        final var policy = new CertificatePolicyModel();
        policy.setLifetimeActions(List.of(lifetimeActivity(EMAIL_CONTACTS, daysBeforeExpiryTrigger(ONE_HUNDRED))));
        request.setPolicy(policy);

        //when
        final var actual = underTest
                .importCertificate(name, VAULT_URI_1, request);

        //then
        Assertions.assertEquals(OK, actual.getStatusCode());
        final var body = actual.getBody();
        Assertions.assertNotNull(body);
        Assertions.assertNotNull(body.getCertificate());
        Assertions.assertNotNull(body.getThumbprint());
        final var lifetimeActions = body.getPolicy().getLifetimeActions();
        Assertions.assertNotNull(lifetimeActions);
        Assertions.assertEquals(1, lifetimeActions.size());
        final var lifetimeActionModel = lifetimeActions.getFirst();
        Assertions.assertEquals(EMAIL_CONTACTS, lifetimeActionModel.getAction());
        Assertions.assertEquals(ONE_HUNDRED, lifetimeActionModel.getTrigger().getDaysBeforeExpiry());
    }

    @Test
    void testImportCertificateShouldThrowExceptionWhenCalledWithNotMatchingCertTypes() {
        //given
        final var request = getCreateImportRequest("/cert/ec.p12", CertContentType.PEM);
        final var name = CERT_NAME_3 + "-import-invalid";

        //when
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> underTest.importCertificate(name, VAULT_URI_1, request));

        //then + exception
    }

    @Test
    void testImportCertificateShouldThrowExceptionWhenCalledWithInvalidLifetimeActionsTriggeringZeroDaysBeforeExpiry() {
        //given
        final var request = getCreateImportRequest("/cert/ec.p12", CertContentType.PKCS12);
        final var name = CERT_NAME_3 + "-import-invalid";
        request.getPolicy().setLifetimeActions(List.of(lifetimeActivity(EMAIL_CONTACTS, daysBeforeExpiryTrigger(0))));

        //when
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> underTest.importCertificate(name, VAULT_URI_1, request));

        //then + exception
    }

    @Test
    void testImportCertificateShouldThrowExceptionWhenCalledWithInvalidLifetimeActionsTriggeringTooManyDaysBeforeExpiry() {
        //given
        final var request = getCreateImportRequest("/cert/ec.p12", CertContentType.PKCS12);
        final var name = CERT_NAME_3 + "-import-invalid";
        request.getPolicy().setLifetimeActions(List.of(lifetimeActivity(EMAIL_CONTACTS, daysBeforeExpiryTrigger(A_HUNDRED_YEARS))));

        //when
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> underTest.importCertificate(name, VAULT_URI_1, request));

        //then + exception
    }

    @Test
    void testImportCertificateShouldThrowExceptionWhenCalledWithInvalidLifetimeActionsAskingForAutoRenewal() {
        //given
        final var request = getCreateImportRequest("/cert/ec.p12", CertContentType.PKCS12);
        final var name = CERT_NAME_3 + "-import-invalid";
        request.getPolicy().setLifetimeActions(List.of(lifetimeActivity(AUTO_RENEW, daysBeforeExpiryTrigger(ONE_HUNDRED))));

        //when
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> underTest.importCertificate(name, VAULT_URI_1, request));

        //then + exception
    }

    @Test
    void testImportCertificateShouldThrowExceptionWhenCalledWithInvalidLifetimeActionsTriggeringAtZeroPercent() {
        //given
        final var request = getCreateImportRequest("/cert/ec.p12", CertContentType.PKCS12);
        final var name = CERT_NAME_3 + "-import-invalid";
        request.getPolicy().setLifetimeActions(List.of(lifetimeActivity(EMAIL_CONTACTS, lifeTimePercentageTrigger(0))));

        //when
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> underTest.importCertificate(name, VAULT_URI_1, request));

        //then + exception
    }

    @Test
    void testImportCertificateShouldThrowExceptionWhenCalledWithInvalidLifetimeActionsTriggeringAtAHundredPercents() {
        //given
        final var request = getCreateImportRequest("/cert/ec.p12", CertContentType.PKCS12);
        final var name = CERT_NAME_3 + "-import-invalid";
        request.getPolicy().setLifetimeActions(List.of(lifetimeActivity(EMAIL_CONTACTS, lifeTimePercentageTrigger(ONE_HUNDRED))));

        //when
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> underTest.importCertificate(name, VAULT_URI_1, request));

        //then + exception
    }

    @Test
    void testImportCertificateShouldThrowExceptionWhenCalledWithInvalidLifetimeActionsWithTwoActions() {
        //given
        final var request = getCreateImportRequest("/cert/ec.p12", CertContentType.PKCS12);
        final var name = CERT_NAME_3 + "-import-invalid";
        request.getPolicy().setLifetimeActions(List.of(
                lifetimeActivity(AUTO_RENEW, daysBeforeExpiryTrigger(1)),
                lifetimeActivity(EMAIL_CONTACTS, daysBeforeExpiryTrigger(1))
        ));

        //when
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> underTest.importCertificate(name, VAULT_URI_1, request));

        //then + exception
    }

    @Test
    void testVersionsShouldReturnAPageOfVersionsWhenTheCertificateExists() {
        //given
        final var request = getCreateImportRequest("/cert/ec.pem", CertContentType.PEM);
        final var name = CERT_NAME_1 + "-versions";
        final var imported = Objects
                .requireNonNull(underTest.importCertificate(name, VAULT_URI_1, request).getBody());

        //when
        final var actual = underTest
                .versions(name, VAULT_URI_1, 1, 0);

        //then
        Assertions.assertEquals(OK, actual.getStatusCode());
        Assertions.assertNotNull(actual.getBody());
        Assertions.assertNull(actual.getBody().getNextLink());
        final var list = actual.getBody().getValue();
        Assertions.assertEquals(1, list.size());
        final var item = list.getFirst();
        Assertions.assertEquals(imported.getId(), item.getCertificateId());
        Assertions.assertArrayEquals(imported.getThumbprint(), item.getThumbprint());
        Assertions.assertEquals(imported.getAttributes(), item.getAttributes());
    }

    @ParameterizedTest
    @MethodSource("certificateListProvider")
    void testListCertificatesShouldReturnAPageOfVersionsWhenTheCertificateExists(
            final URI vault, final int pageSize, final int offset, final String nextLink) {
        //given
        create(vault);
        final var request = getCreateImportRequest("/cert/ec.pem", CertContentType.PEM);
        final var namePrefix = CERT_NAME_1;
        final var certCount = 10;
        final var fullList = IntStream.range(0, certCount)
                .mapToObj(i -> namePrefix + i)
                .collect(Collectors.toMap(Function.identity(), n -> Objects
                        .requireNonNull(underTest.importCertificate(n, vault, request).getBody())));

        //when
        final var actual = underTest
                .listCertificates(vault, pageSize, offset, true);

        //then
        Assertions.assertEquals(OK, actual.getStatusCode());
        Assertions.assertNotNull(actual.getBody());
        Assertions.assertEquals(nextLink, actual.getBody().getNextLink());
        final var list = actual.getBody().getValue();
        final var expectedSize = Math.min(pageSize, certCount - offset);
        Assertions.assertEquals(expectedSize, list.size());
        for (var i = 0; i < expectedSize; i++) {
            final var item = list.get(i);
            final var expected = fullList.get(namePrefix + (offset + i));
            Assertions.assertEquals(expected.getId().replaceAll("/[0-9a-f]+$", ""), item.getCertificateId());
            Assertions.assertArrayEquals(expected.getThumbprint(), item.getThumbprint());
            Assertions.assertEquals(expected.getAttributes(), item.getAttributes());
        }
    }

    @Test
    void testDeleteShouldReturnDeleteModelWhenCalledWithValidData() {
        //given
        final var request = getCreateCertificateRequest();
        final var certificateName = CERT_NAME_2 + "-delete";
        underTest.create(certificateName, VAULT_URI_1, request);
        final var certificateVaultFake = findByUri(VAULT_URI_1)
                .certificateVaultFake();
        final var versions = certificateVaultFake
                .getEntities()
                .getVersions(new CertificateEntityId(VAULT_URI_1, certificateName));

        //when
        final var actual = underTest.delete(certificateName, VAULT_URI_1);

        //then
        Assertions.assertEquals(OK, actual.getStatusCode());
        final var body = actual.getBody();
        final var expectedId =
                new VersionedCertificateEntityId(VAULT_URI_1, certificateName, versions.getFirst());

        assertExpectedCertificateModel(request, expectedId, body);
        assertIsDeletedModel(body, expectedId);
        Assertions.assertFalse(certificateVaultFake.getEntities().containsName(certificateName));
        Assertions.assertTrue(certificateVaultFake.getDeletedEntities().containsName(certificateName));
    }

    @Test
    void testPurgeDeletedShouldRemoveEntityFromDeletedMapWhenCalledWithValidData() {
        //given
        final var request = getCreateCertificateRequest();
        final var certificateName = CERT_NAME_2 + "-purge";
        underTest.create(certificateName, VAULT_URI_1, request);
        final var certificateVaultFake = findByUri(VAULT_URI_1)
                .certificateVaultFake();
        certificateVaultFake.delete(new CertificateEntityId(VAULT_URI_1, certificateName));

        //when
        final var actual = underTest.purgeDeleted(certificateName, VAULT_URI_1);

        //then
        Assertions.assertEquals(NO_CONTENT, actual.getStatusCode());
        Assertions.assertFalse(certificateVaultFake.getEntities().containsName(certificateName));
        Assertions.assertFalse(certificateVaultFake.getDeletedEntities().containsName(certificateName));
    }

    @Test
    void testRecoverDeletedCertificateShouldReturnRecoveredEntityWhenCalledWithValidData() {
        //given
        final var request = getCreateCertificateRequest();
        final var certificateName = CERT_NAME_2 + "-recover";
        underTest.create(certificateName, VAULT_URI_1, request);
        final var certificateVaultFake = findByUri(VAULT_URI_1)
                .certificateVaultFake();
        final var entityId = new CertificateEntityId(VAULT_URI_1, certificateName);
        final var versions = certificateVaultFake
                .getEntities()
                .getVersions(entityId);
        certificateVaultFake.delete(entityId);

        //when
        final var actual = underTest
                .recoverDeletedCertificate(certificateName, VAULT_URI_1);

        //then
        Assertions.assertEquals(OK, actual.getStatusCode());
        final var body = actual.getBody();
        final var expectedId =
                new VersionedCertificateEntityId(VAULT_URI_1, certificateName, versions.getFirst());

        assertExpectedCertificateModel(request, expectedId, body);
        Assertions.assertTrue(certificateVaultFake.getEntities().containsName(certificateName));
        Assertions.assertFalse(certificateVaultFake.getDeletedEntities().containsName(certificateName));
    }

    @Test
    void testGetDeletedCertificateShouldReturnDeleteModelWhenCalledWithValidData() {
        //given
        final var request = getCreateCertificateRequest();
        final var certificateName = CERT_NAME_2 + "-get-deleted";
        underTest.create(certificateName, VAULT_URI_1, request);
        final var certificateVaultFake = findByUri(VAULT_URI_1)
                .certificateVaultFake();
        final var entityId = new CertificateEntityId(VAULT_URI_1, certificateName);
        final var versions = certificateVaultFake
                .getEntities()
                .getVersions(entityId);
        certificateVaultFake.delete(entityId);

        //when
        final var actual = underTest
                .getDeletedCertificate(certificateName, VAULT_URI_1);

        //then
        Assertions.assertEquals(OK, actual.getStatusCode());
        final var body = actual.getBody();
        final var expectedId =
                new VersionedCertificateEntityId(VAULT_URI_1, certificateName, versions.getFirst());

        assertExpectedCertificateModel(request, expectedId, body);
        assertIsDeletedModel(body, expectedId);
        Assertions.assertFalse(certificateVaultFake.getEntities().containsName(certificateName));
        Assertions.assertTrue(certificateVaultFake.getDeletedEntities().containsName(certificateName));
    }

    @Test
    void testListDeletedCertificatesShouldReturnDeletedItemModelsWhenCalledWithValidData() {
        //given
        final var request = getCreateCertificateRequest();
        final var certificateName = CERT_NAME_2;
        underTest.create(certificateName, VAULT_URI_2, request);
        final var certificateVaultFake = findByUri(VAULT_URI_2)
                .certificateVaultFake();
        final var entityId = new CertificateEntityId(VAULT_URI_2, certificateName);
        certificateVaultFake.delete(entityId);

        //when
        final var actual =
                underTest.listDeletedCertificates(VAULT_URI_2, 1, 0, true);

        //then
        Assertions.assertEquals(OK, actual.getStatusCode());
        final var body = actual.getBody();
        Assertions.assertNotNull(body);
        Assertions.assertNull(body.getNextLink());
        Assertions.assertEquals(1, body.getValue().size());
        final var itemModel = body.getValue().getFirst();
        Assertions.assertEquals(entityId.asUriNoVersion(VAULT_URI_2).toString(), itemModel.getCertificateId());
        assertIsDeletedModel(itemModel, entityId);
    }

    @Test
    void testUpdateShouldReturnModelWhenCalledWithValidData() {
        //given
        final var request = getCreateCertificateRequest();
        final var certificateName = CERT_NAME_2 + "-update-properties";
        underTest.create(certificateName, VAULT_URI_1, request);
        final var versions = findByUri(VAULT_URI_1)
                .certificateVaultFake()
                .getEntities()
                .getVersions(new CertificateEntityId(VAULT_URI_1, certificateName));
        final var properties = new UpdateCertificateRequest();
        final var attributes = new CertificatePropertiesModel();
        attributes.setEnabled(false);
        properties.setTags(TestConstants.TAGS_THREE_KEYS);
        properties.setAttributes(attributes);

        //when
        final var actual = underTest
                .updateCertificateProperties(certificateName, versions.getLast(), VAULT_URI_1, properties);

        //then
        Assertions.assertEquals(OK, actual.getStatusCode());
        final var body = actual.getBody();
        Assertions.assertNotNull(body);
        //policy is removed from the response
        Assertions.assertNull(body.getPolicy());
        Assertions.assertEquals(TestConstants.TAGS_THREE_KEYS, body.getTags());
        Assertions.assertNotEquals(attributes, body.getAttributes());
        Assertions.assertEquals(attributes.isEnabled(), body.getAttributes().isEnabled());
        Assertions.assertNotNull(body.getAttributes().getCreatedOn());
        Assertions.assertNotNull(body.getAttributes().getUpdatedOn());
        Assertions.assertNotNull(body.getAttributes().getNotBefore());
        Assertions.assertNotNull(body.getAttributes().getExpiresOn());
    }

    private CertificateImportRequest getCreateImportRequest(final String resource, final CertContentType type) {
        final var request = new CertificateImportRequest();
        request.setCertificate(ResourceUtils.loadResourceAsByteArray(resource));
        if (resource.endsWith("p12")) {
            request.setPassword("changeit");
        }
        if (type != null) {
            final var secretProperties = new CertificateSecretModel();
            secretProperties.setContentType(type.getMimeType());
            final var policy = new CertificatePolicyModel();
            policy.setSecretProperties(secretProperties);
            request.setPolicy(policy);
        }
        return request;
    }

    private void assertExpectedCertificateModel(
            final CreateCertificateRequest request,
            final VersionedCertificateEntityId expectedId,
            final KeyVaultCertificateModel body) {
        Assertions.assertNotNull(body);
        final var id = expectedId.asUri(VAULT_URI_1);
        Assertions.assertEquals(id.toString(), body.getId());
        Assertions.assertEquals(request.getPolicy().getSecretProperties(), body.getPolicy().getSecretProperties());
        Assertions.assertEquals(request.getPolicy().getKeyProperties(), body.getPolicy().getKeyProperties());
        final var x509Properties = request.getPolicy().getX509Properties();
        x509Properties.setExtendedKeyUsage(DEFAULT_EXT_KEY_USAGES);
        x509Properties.setKeyUsage(DEFAULT_EC_KEY_USAGES);
        Assertions.assertEquals(x509Properties, body.getPolicy().getX509Properties());
        Assertions.assertTrue(body.getAttributes().isEnabled());
        Assertions.assertTrue(body.getAttributes().getRecoveryLevel().isPurgeable());
        Assertions.assertTrue(body.getAttributes().getRecoveryLevel().isRecoverable());
    }

    private void assertIsDeletedModel(final DeletedModel body, final CertificateEntityId expectedId) {
        final var recoveryUri = expectedId.asRecoveryUri(expectedId.vault());
        Assertions.assertNotNull(body);
        Assertions.assertEquals(recoveryUri.toString(), body.getRecoveryId());
        Assertions.assertNotNull(body.getDeletedDate());
        Assertions.assertNotNull(body.getScheduledPurgeDate());
    }

    private CertificateLifetimeActionModel lifetimeActivity(
            final CertificateLifetimeActionActivity action, final CertificateLifetimeActionTriggerModel trigger) {
        final var activity = new CertificateLifetimeActionModel();
        activity.setAction(action);
        activity.setTrigger(trigger);
        return activity;
    }

    private CertificateLifetimeActionTriggerModel lifeTimePercentageTrigger(final int value) {
        final var trigger = new CertificateLifetimeActionTriggerModel();
        trigger.setLifetimePercentage(value);
        return trigger;
    }

    private CertificateLifetimeActionTriggerModel daysBeforeExpiryTrigger(final int value) {
        final var trigger = new CertificateLifetimeActionTriggerModel();
        trigger.setDaysBeforeExpiry(value);
        return trigger;
    }
}
