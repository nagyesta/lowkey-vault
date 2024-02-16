package com.github.nagyesta.lowkeyvault.controller.v7_5;

import com.github.nagyesta.abortmission.booster.jupiter.annotation.LaunchAbortArmed;
import com.github.nagyesta.lowkeyvault.ResourceUtils;
import com.github.nagyesta.lowkeyvault.TestConstants;
import com.github.nagyesta.lowkeyvault.controller.v7_3.BaseCertificateControllerIntegrationTest;
import com.github.nagyesta.lowkeyvault.model.common.DeletedModel;
import com.github.nagyesta.lowkeyvault.model.common.KeyVaultItemListModel;
import com.github.nagyesta.lowkeyvault.model.v7_3.certificate.*;
import com.github.nagyesta.lowkeyvault.service.certificate.CertificateLifetimeActionActivity;
import com.github.nagyesta.lowkeyvault.service.certificate.CertificateVaultFake;
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
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.github.nagyesta.lowkeyvault.TestConstantsCertificates.*;
import static com.github.nagyesta.lowkeyvault.TestConstantsUri.getRandomVaultUri;
import static com.github.nagyesta.lowkeyvault.TestConstantsUri.getRandomVaultUriAsString;
import static com.github.nagyesta.lowkeyvault.model.common.ApiConstants.V_7_5;
import static com.github.nagyesta.lowkeyvault.service.certificate.CertificateLifetimeActionActivity.AUTO_RENEW;
import static com.github.nagyesta.lowkeyvault.service.certificate.CertificateLifetimeActionActivity.EMAIL_CONTACTS;
import static org.springframework.http.HttpStatus.*;

@LaunchAbortArmed
@SpringBootTest
class CertificateControllerIntegrationTest extends BaseCertificateControllerIntegrationTest {

    private static final int A_HUNDRED_YEARS = 36500;
    private static final int ONE_HUNDRED = 100;
    private static final URI VAULT_URI_1 = getRandomVaultUri();
    private static final URI VAULT_URI_2 = getRandomVaultUri();
    @Autowired
    @Qualifier("CertificateControllerV75")
    private CertificateController underTest;

    @BeforeEach
    void setUp() {
        prepareVaultIfNotExists(VAULT_URI_1);
        prepareVaultIfNotExists(VAULT_URI_2);
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    public static Stream<Arguments> certificateListProvider() {
        final String uri1 = getRandomVaultUriAsString();
        final String uri2 = getRandomVaultUriAsString();
        final String uri3 = getRandomVaultUriAsString();
        return Stream.<Arguments>builder()
                .add(Arguments.of(uri1, 5, 0,
                        uri1 + "/certificates?api-version=7.5&$skiptoken=5&maxresults=5&includePending=true"))
                .add(Arguments.of(uri2, 3, 1,
                        uri2 + "/certificates?api-version=7.5&$skiptoken=4&maxresults=3&includePending=true"))
                .add(Arguments.of(uri3, 3, 8,
                        null))
                .build();
    }

    @Test
    void testCreateShouldReturnPendingCertificateWhenCalled() {
        //given
        final CreateCertificateRequest request = getCreateCertificateRequest();

        //when
        final ResponseEntity<KeyVaultPendingCertificateModel> actual = underTest
                .create("create-" + CERT_NAME_1, VAULT_URI_1, request);

        //then
        Assertions.assertEquals(ACCEPTED, actual.getStatusCode());
        final KeyVaultPendingCertificateModel body = actual.getBody();
        assertPendingCreateResponseIsAsExpected(body);
    }

    @Test
    void testCreateShouldThrowExceptionWhenCalledWithInvalidLifetimeActionsWithZeroPercentageTrigger() {
        //given
        final CreateCertificateRequest request = getCreateCertificateRequest();
        final List<CertificateLifetimeActionModel> actions = List.of(
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
        final CreateCertificateRequest request = getCreateCertificateRequest();
        final List<CertificateLifetimeActionModel> actions = List.of(
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
        final CreateCertificateRequest request = getCreateCertificateRequest();
        final List<CertificateLifetimeActionModel> actions = List.of(
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
        final CreateCertificateRequest request = getCreateCertificateRequest();
        request.getPolicy().setLifetimeActions(List.of(lifetimeActivity(EMAIL_CONTACTS, daysBeforeExpiryTrigger(1))));

        //when
        final ResponseEntity<KeyVaultPendingCertificateModel> actual = underTest
                .create("create-lifetime-" + CERT_NAME_1, VAULT_URI_1, request);

        //then
        Assertions.assertEquals(ACCEPTED, actual.getStatusCode());
        final KeyVaultPendingCertificateModel body = actual.getBody();
        assertPendingCreateResponseIsAsExpected(body);
    }

    @Test
    void testGetShouldReturnModelWhenCalledWithValidData() {
        //given
        final CreateCertificateRequest request = getCreateCertificateRequest();
        underTest.create(CERT_NAME_2, VAULT_URI_1, request);
        final Deque<String> versions = findByUri(VAULT_URI_1)
                .certificateVaultFake()
                .getEntities()
                .getVersions(new CertificateEntityId(VAULT_URI_1, CERT_NAME_2));

        //when
        final ResponseEntity<KeyVaultCertificateModel> actual = underTest.get(CERT_NAME_2, VAULT_URI_1);

        //then
        Assertions.assertEquals(OK, actual.getStatusCode());
        final KeyVaultCertificateModel body = actual.getBody();
        Assertions.assertNotNull(body);
        Assertions.assertNotNull(body.getCertificate());
        Assertions.assertNotNull(body.getThumbprint());
        Assertions.assertEquals(Collections.emptyMap(), body.getTags());
        final URI id = new VersionedCertificateEntityId(VAULT_URI_1, CERT_NAME_2, versions.getFirst())
                .asUri(VAULT_URI_1);
        Assertions.assertEquals(id.toString(), body.getId());
    }

    @Test
    void testGetWithVersionShouldReturnModelWhenCalledWithValidData() {
        //given
        final CreateCertificateRequest request = getCreateCertificateRequest();
        underTest.create(CERT_NAME_3, VAULT_URI_1, request);
        final Deque<String> versions = findByUri(VAULT_URI_1)
                .certificateVaultFake()
                .getEntities()
                .getVersions(new CertificateEntityId(VAULT_URI_1, CERT_NAME_3));

        //when
        final ResponseEntity<KeyVaultCertificateModel> actual = underTest
                .getWithVersion(CERT_NAME_3, versions.getFirst(), VAULT_URI_1);

        //then
        Assertions.assertEquals(OK, actual.getStatusCode());
        final KeyVaultCertificateModel body = actual.getBody();
        Assertions.assertNotNull(body);
        Assertions.assertNotNull(body.getCertificate());
        Assertions.assertNotNull(body.getThumbprint());
        Assertions.assertEquals(Collections.emptyMap(), body.getTags());
        final URI id = new VersionedCertificateEntityId(VAULT_URI_1, CERT_NAME_3, versions.getFirst())
                .asUri(VAULT_URI_1);
        Assertions.assertEquals(id.toString(), body.getId());
    }

    @Test
    void testApiVersionShouldReturnV75WhenCalled() {
        //given

        //when
        final String actual = underTest.apiVersion();

        //then
        Assertions.assertEquals(V_7_5, actual);
    }

    @Test
    void testImportCertificateShouldReturnModelWhenCalledWithValidPemData() {
        //given
        final CertificateImportRequest request = getCreateImportRequest("/cert/ec.pem", CertContentType.PEM);
        final String name = CERT_NAME_3 + "-import-pem";

        //when
        final ResponseEntity<KeyVaultCertificateModel> actual = underTest
                .importCertificate(name, VAULT_URI_1, request);

        //then
        Assertions.assertEquals(OK, actual.getStatusCode());
        final KeyVaultCertificateModel body = actual.getBody();
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
        final CertificateImportRequest request = getCreateImportRequest("/cert/ec.p12", CertContentType.PKCS12);
        final String name = CERT_NAME_3 + "-import-pkcs";

        //when
        final ResponseEntity<KeyVaultCertificateModel> actual = underTest
                .importCertificate(name, VAULT_URI_1, request);

        //then
        Assertions.assertEquals(OK, actual.getStatusCode());
        final KeyVaultCertificateModel body = actual.getBody();
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
        final CertificateImportRequest request = getCreateImportRequest("/cert/ec.pem", null);
        final String name = CERT_NAME_3 + "-import-pem-auto";

        //when
        final ResponseEntity<KeyVaultCertificateModel> actual = underTest
                .importCertificate(name, VAULT_URI_1, request);

        //then
        Assertions.assertEquals(OK, actual.getStatusCode());
        final KeyVaultCertificateModel body = actual.getBody();
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
        final CertificateImportRequest request = getCreateImportRequest("/cert/ec.p12", null);
        final String name = CERT_NAME_3 + "-import-pkcs-auto";

        //when
        final ResponseEntity<KeyVaultCertificateModel> actual = underTest
                .importCertificate(name, VAULT_URI_1, request);

        //then
        Assertions.assertEquals(OK, actual.getStatusCode());
        final KeyVaultCertificateModel body = actual.getBody();
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
        final CertificateImportRequest request = getCreateImportRequest("/cert/ec.p12", null);
        final String name = CERT_NAME_3 + "-import-pkcs-lifetime";
        final CertificatePolicyModel policy = new CertificatePolicyModel();
        policy.setLifetimeActions(List.of(lifetimeActivity(EMAIL_CONTACTS, daysBeforeExpiryTrigger(ONE_HUNDRED))));
        request.setPolicy(policy);

        //when
        final ResponseEntity<KeyVaultCertificateModel> actual = underTest
                .importCertificate(name, VAULT_URI_1, request);

        //then
        Assertions.assertEquals(OK, actual.getStatusCode());
        final KeyVaultCertificateModel body = actual.getBody();
        Assertions.assertNotNull(body);
        Assertions.assertNotNull(body.getCertificate());
        Assertions.assertNotNull(body.getThumbprint());
        final List<CertificateLifetimeActionModel> lifetimeActions = body.getPolicy().getLifetimeActions();
        Assertions.assertNotNull(lifetimeActions);
        Assertions.assertEquals(1, lifetimeActions.size());
        final CertificateLifetimeActionModel lifetimeActionModel = lifetimeActions.get(0);
        Assertions.assertEquals(EMAIL_CONTACTS, lifetimeActionModel.getAction());
        Assertions.assertEquals(ONE_HUNDRED, lifetimeActionModel.getTrigger().getDaysBeforeExpiry());
    }

    @Test
    void testImportCertificateShouldThrowExceptionWhenCalledWithNotMatchingCertTypes() {
        //given
        final CertificateImportRequest request = getCreateImportRequest("/cert/ec.p12", CertContentType.PEM);
        final String name = CERT_NAME_3 + "-import-invalid";

        //when
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> underTest.importCertificate(name, VAULT_URI_1, request));

        //then + exception
    }

    @Test
    void testImportCertificateShouldThrowExceptionWhenCalledWithInvalidLifetimeActionsTriggeringZeroDaysBeforeExpiry() {
        //given
        final CertificateImportRequest request = getCreateImportRequest("/cert/ec.p12", CertContentType.PKCS12);
        final String name = CERT_NAME_3 + "-import-invalid";
        request.getPolicy().setLifetimeActions(List.of(lifetimeActivity(EMAIL_CONTACTS, daysBeforeExpiryTrigger(0))));

        //when
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> underTest.importCertificate(name, VAULT_URI_1, request));

        //then + exception
    }

    @Test
    void testImportCertificateShouldThrowExceptionWhenCalledWithInvalidLifetimeActionsTriggeringTooManyDaysBeforeExpiry() {
        //given
        final CertificateImportRequest request = getCreateImportRequest("/cert/ec.p12", CertContentType.PKCS12);
        final String name = CERT_NAME_3 + "-import-invalid";
        request.getPolicy().setLifetimeActions(List.of(lifetimeActivity(EMAIL_CONTACTS, daysBeforeExpiryTrigger(A_HUNDRED_YEARS))));

        //when
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> underTest.importCertificate(name, VAULT_URI_1, request));

        //then + exception
    }

    @Test
    void testImportCertificateShouldThrowExceptionWhenCalledWithInvalidLifetimeActionsAskingForAutoRenewal() {
        //given
        final CertificateImportRequest request = getCreateImportRequest("/cert/ec.p12", CertContentType.PKCS12);
        final String name = CERT_NAME_3 + "-import-invalid";
        request.getPolicy().setLifetimeActions(List.of(lifetimeActivity(AUTO_RENEW, daysBeforeExpiryTrigger(ONE_HUNDRED))));

        //when
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> underTest.importCertificate(name, VAULT_URI_1, request));

        //then + exception
    }

    @Test
    void testImportCertificateShouldThrowExceptionWhenCalledWithInvalidLifetimeActionsTriggeringAtZeroPercent() {
        //given
        final CertificateImportRequest request = getCreateImportRequest("/cert/ec.p12", CertContentType.PKCS12);
        final String name = CERT_NAME_3 + "-import-invalid";
        request.getPolicy().setLifetimeActions(List.of(lifetimeActivity(EMAIL_CONTACTS, lifeTimePercentageTrigger(0))));

        //when
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> underTest.importCertificate(name, VAULT_URI_1, request));

        //then + exception
    }

    @Test
    void testImportCertificateShouldThrowExceptionWhenCalledWithInvalidLifetimeActionsTriggeringAtAHundredPercents() {
        //given
        final CertificateImportRequest request = getCreateImportRequest("/cert/ec.p12", CertContentType.PKCS12);
        final String name = CERT_NAME_3 + "-import-invalid";
        request.getPolicy().setLifetimeActions(List.of(lifetimeActivity(EMAIL_CONTACTS, lifeTimePercentageTrigger(ONE_HUNDRED))));

        //when
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> underTest.importCertificate(name, VAULT_URI_1, request));

        //then + exception
    }

    @Test
    void testImportCertificateShouldThrowExceptionWhenCalledWithInvalidLifetimeActionsWithTwoActions() {
        //given
        final CertificateImportRequest request = getCreateImportRequest("/cert/ec.p12", CertContentType.PKCS12);
        final String name = CERT_NAME_3 + "-import-invalid";
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
        final CertificateImportRequest request = getCreateImportRequest("/cert/ec.pem", CertContentType.PEM);
        final String name = CERT_NAME_1 + "-versions";
        final KeyVaultCertificateModel imported = Objects
                .requireNonNull(underTest.importCertificate(name, VAULT_URI_1, request).getBody());

        //when
        final ResponseEntity<KeyVaultItemListModel<KeyVaultCertificateItemModel>> actual = underTest
                .versions(name, VAULT_URI_1, 1, 0);

        //then
        Assertions.assertEquals(OK, actual.getStatusCode());
        Assertions.assertNotNull(actual.getBody());
        Assertions.assertNull(actual.getBody().getNextLink());
        final List<KeyVaultCertificateItemModel> list = actual.getBody().getValue();
        Assertions.assertEquals(1, list.size());
        final KeyVaultCertificateItemModel item = list.get(0);
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
        final CertificateImportRequest request = getCreateImportRequest("/cert/ec.pem", CertContentType.PEM);
        final String namePrefix = CERT_NAME_1;
        final int certCount = 10;
        final Map<String, KeyVaultCertificateModel> fullList = IntStream.range(0, certCount)
                .mapToObj(i -> namePrefix + i)
                .collect(Collectors.toMap(Function.identity(), n -> Objects
                        .requireNonNull(underTest.importCertificate(n, vault, request).getBody())));

        //when
        final ResponseEntity<KeyVaultItemListModel<KeyVaultCertificateItemModel>> actual = underTest
                .listCertificates(vault, pageSize, offset, true);

        //then
        Assertions.assertEquals(OK, actual.getStatusCode());
        Assertions.assertNotNull(actual.getBody());
        Assertions.assertEquals(nextLink, actual.getBody().getNextLink());
        final List<KeyVaultCertificateItemModel> list = actual.getBody().getValue();
        final int expectedSize = Math.min(pageSize, certCount - offset);
        Assertions.assertEquals(expectedSize, list.size());
        for (int i = 0; i < expectedSize; i++) {
            final KeyVaultCertificateItemModel item = list.get(i);
            final KeyVaultCertificateModel expected = fullList.get(namePrefix + (offset + i));
            Assertions.assertEquals(expected.getId().replaceAll("/[0-9a-f]+$", ""), item.getCertificateId());
            Assertions.assertArrayEquals(expected.getThumbprint(), item.getThumbprint());
            Assertions.assertEquals(expected.getAttributes(), item.getAttributes());
        }
    }

    @Test
    void testDeleteShouldReturnDeleteModelWhenCalledWithValidData() {
        //given
        final CreateCertificateRequest request = getCreateCertificateRequest();
        final String certificateName = CERT_NAME_2 + "-delete";
        underTest.create(certificateName, VAULT_URI_1, request);
        final CertificateVaultFake certificateVaultFake = findByUri(VAULT_URI_1)
                .certificateVaultFake();
        final Deque<String> versions = certificateVaultFake
                .getEntities()
                .getVersions(new CertificateEntityId(VAULT_URI_1, certificateName));

        //when
        final ResponseEntity<DeletedKeyVaultCertificateModel> actual = underTest.delete(certificateName, VAULT_URI_1);

        //then
        Assertions.assertEquals(OK, actual.getStatusCode());
        final DeletedKeyVaultCertificateModel body = actual.getBody();
        final VersionedCertificateEntityId expectedId =
                new VersionedCertificateEntityId(VAULT_URI_1, certificateName, versions.getFirst());

        assertExpectedCertificateModel(request, expectedId, body);
        assertIsDeletedModel(body, expectedId);
        Assertions.assertFalse(certificateVaultFake.getEntities().containsName(certificateName));
        Assertions.assertTrue(certificateVaultFake.getDeletedEntities().containsName(certificateName));
    }

    @Test
    void testPurgeDeletedShouldRemoveEntityFromDeletedMapWhenCalledWithValidData() {
        //given
        final CreateCertificateRequest request = getCreateCertificateRequest();
        final String certificateName = CERT_NAME_2 + "-purge";
        underTest.create(certificateName, VAULT_URI_1, request);
        final CertificateVaultFake certificateVaultFake = findByUri(VAULT_URI_1)
                .certificateVaultFake();
        certificateVaultFake.delete(new CertificateEntityId(VAULT_URI_1, certificateName));

        //when
        final ResponseEntity<Void> actual = underTest.purgeDeleted(certificateName, VAULT_URI_1);

        //then
        Assertions.assertEquals(NO_CONTENT, actual.getStatusCode());
        Assertions.assertFalse(certificateVaultFake.getEntities().containsName(certificateName));
        Assertions.assertFalse(certificateVaultFake.getDeletedEntities().containsName(certificateName));
    }

    @Test
    void testRecoverDeletedCertificateShouldReturnRecoveredEntityWhenCalledWithValidData() {
        //given
        final CreateCertificateRequest request = getCreateCertificateRequest();
        final String certificateName = CERT_NAME_2 + "-recover";
        underTest.create(certificateName, VAULT_URI_1, request);
        final CertificateVaultFake certificateVaultFake = findByUri(VAULT_URI_1)
                .certificateVaultFake();
        final CertificateEntityId entityId = new CertificateEntityId(VAULT_URI_1, certificateName);
        final Deque<String> versions = certificateVaultFake
                .getEntities()
                .getVersions(entityId);
        certificateVaultFake.delete(entityId);

        //when
        final ResponseEntity<KeyVaultCertificateModel> actual = underTest
                .recoverDeletedCertificate(certificateName, VAULT_URI_1);

        //then
        Assertions.assertEquals(OK, actual.getStatusCode());
        final KeyVaultCertificateModel body = actual.getBody();
        final VersionedCertificateEntityId expectedId =
                new VersionedCertificateEntityId(VAULT_URI_1, certificateName, versions.getFirst());

        assertExpectedCertificateModel(request, expectedId, body);
        Assertions.assertTrue(certificateVaultFake.getEntities().containsName(certificateName));
        Assertions.assertFalse(certificateVaultFake.getDeletedEntities().containsName(certificateName));
    }

    @Test
    void testGetDeletedCertificateShouldReturnDeleteModelWhenCalledWithValidData() {
        //given
        final CreateCertificateRequest request = getCreateCertificateRequest();
        final String certificateName = CERT_NAME_2 + "-get-deleted";
        underTest.create(certificateName, VAULT_URI_1, request);
        final CertificateVaultFake certificateVaultFake = findByUri(VAULT_URI_1)
                .certificateVaultFake();
        final CertificateEntityId entityId = new CertificateEntityId(VAULT_URI_1, certificateName);
        final Deque<String> versions = certificateVaultFake
                .getEntities()
                .getVersions(entityId);
        certificateVaultFake.delete(entityId);

        //when
        final ResponseEntity<DeletedKeyVaultCertificateModel> actual = underTest
                .getDeletedCertificate(certificateName, VAULT_URI_1);

        //then
        Assertions.assertEquals(OK, actual.getStatusCode());
        final DeletedKeyVaultCertificateModel body = actual.getBody();
        final VersionedCertificateEntityId expectedId =
                new VersionedCertificateEntityId(VAULT_URI_1, certificateName, versions.getFirst());

        assertExpectedCertificateModel(request, expectedId, body);
        assertIsDeletedModel(body, expectedId);
        Assertions.assertFalse(certificateVaultFake.getEntities().containsName(certificateName));
        Assertions.assertTrue(certificateVaultFake.getDeletedEntities().containsName(certificateName));
    }

    @Test
    void testListDeletedCertificatesShouldReturnDeletedItemModelsWhenCalledWithValidData() {
        //given
        final CreateCertificateRequest request = getCreateCertificateRequest();
        final String certificateName = CERT_NAME_2;
        underTest.create(certificateName, VAULT_URI_2, request);
        final CertificateVaultFake certificateVaultFake = findByUri(VAULT_URI_2)
                .certificateVaultFake();
        final CertificateEntityId entityId = new CertificateEntityId(VAULT_URI_2, certificateName);
        certificateVaultFake.delete(entityId);

        //when
        final ResponseEntity<KeyVaultItemListModel<DeletedKeyVaultCertificateItemModel>> actual =
                underTest.listDeletedCertificates(VAULT_URI_2, 1, 0, true);

        //then
        Assertions.assertEquals(OK, actual.getStatusCode());
        final KeyVaultItemListModel<DeletedKeyVaultCertificateItemModel> body = actual.getBody();
        Assertions.assertNotNull(body);
        Assertions.assertNull(body.getNextLink());
        Assertions.assertEquals(1, body.getValue().size());
        final DeletedKeyVaultCertificateItemModel itemModel = body.getValue().get(0);
        Assertions.assertEquals(entityId.asUriNoVersion(VAULT_URI_2).toString(), itemModel.getCertificateId());
        assertIsDeletedModel(itemModel, entityId);
    }

    @Test
    void testUpdateShouldReturnModelWhenCalledWithValidData() {
        //given
        final CreateCertificateRequest request = getCreateCertificateRequest();
        final String certificateName = CERT_NAME_2 + "-update-properties";
        underTest.create(certificateName, VAULT_URI_1, request);
        final Deque<String> versions = findByUri(VAULT_URI_1)
                .certificateVaultFake()
                .getEntities()
                .getVersions(new CertificateEntityId(VAULT_URI_1, certificateName));
        final UpdateCertificateRequest properties = new UpdateCertificateRequest();
        final CertificatePropertiesModel attributes = new CertificatePropertiesModel();
        attributes.setEnabled(false);
        properties.setTags(TestConstants.TAGS_THREE_KEYS);
        properties.setAttributes(attributes);

        //when
        final ResponseEntity<KeyVaultCertificateModel> actual = underTest
                .updateCertificateProperties(certificateName, versions.getLast(), VAULT_URI_1, properties);

        //then
        Assertions.assertEquals(OK, actual.getStatusCode());
        final KeyVaultCertificateModel body = actual.getBody();
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
        final CertificateImportRequest request = new CertificateImportRequest();
        request.setCertificate(ResourceUtils.loadResourceAsByteArray(resource));
        if (resource.endsWith("p12")) {
            request.setPassword("changeit");
        }
        if (type != null) {
            final CertificateSecretModel secretProperties = new CertificateSecretModel();
            secretProperties.setContentType(type.getMimeType());
            final CertificatePolicyModel policy = new CertificatePolicyModel();
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
        final URI id = expectedId.asUri(VAULT_URI_1);
        Assertions.assertEquals(id.toString(), body.getId());
        Assertions.assertEquals(request.getPolicy().getSecretProperties(), body.getPolicy().getSecretProperties());
        Assertions.assertEquals(request.getPolicy().getKeyProperties(), body.getPolicy().getKeyProperties());
        Assertions.assertEquals(request.getPolicy().getX509Properties(), body.getPolicy().getX509Properties());
        Assertions.assertTrue(body.getAttributes().isEnabled());
        Assertions.assertTrue(body.getAttributes().getRecoveryLevel().isPurgeable());
        Assertions.assertTrue(body.getAttributes().getRecoveryLevel().isRecoverable());
    }

    private void assertIsDeletedModel(final DeletedModel body, final CertificateEntityId expectedId) {
        final URI recoveryUri = expectedId.asRecoveryUri(expectedId.vault());
        Assertions.assertNotNull(body);
        Assertions.assertEquals(recoveryUri.toString(), body.getRecoveryId());
        Assertions.assertNotNull(body.getDeletedDate());
        Assertions.assertNotNull(body.getScheduledPurgeDate());
    }

    private CertificateLifetimeActionModel lifetimeActivity(
            final CertificateLifetimeActionActivity action, final CertificateLifetimeActionTriggerModel trigger) {
        final CertificateLifetimeActionModel activity = new CertificateLifetimeActionModel();
        activity.setAction(action);
        activity.setTrigger(trigger);
        return activity;
    }

    private CertificateLifetimeActionTriggerModel lifeTimePercentageTrigger(final int value) {
        final CertificateLifetimeActionTriggerModel trigger = new CertificateLifetimeActionTriggerModel();
        trigger.setLifetimePercentage(value);
        return trigger;
    }

    private CertificateLifetimeActionTriggerModel daysBeforeExpiryTrigger(final int value) {
        final CertificateLifetimeActionTriggerModel trigger = new CertificateLifetimeActionTriggerModel();
        trigger.setDaysBeforeExpiry(value);
        return trigger;
    }
}
