package com.github.nagyesta.lowkeyvault.controller.v7_3;

import com.github.nagyesta.abortmission.booster.jupiter.annotation.LaunchAbortArmed;
import com.github.nagyesta.lowkeyvault.ResourceUtils;
import com.github.nagyesta.lowkeyvault.model.common.KeyVaultItemListModel;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyCurveName;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyType;
import com.github.nagyesta.lowkeyvault.model.v7_3.certificate.*;
import com.github.nagyesta.lowkeyvault.service.certificate.id.CertificateEntityId;
import com.github.nagyesta.lowkeyvault.service.certificate.id.VersionedCertificateEntityId;
import com.github.nagyesta.lowkeyvault.service.certificate.impl.CertAuthorityType;
import com.github.nagyesta.lowkeyvault.service.certificate.impl.CertContentType;
import com.github.nagyesta.lowkeyvault.service.exception.NotFoundException;
import com.github.nagyesta.lowkeyvault.service.vault.VaultService;
import org.junit.jupiter.api.Assertions;
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
import static com.github.nagyesta.lowkeyvault.TestConstantsUri.*;
import static com.github.nagyesta.lowkeyvault.model.common.ApiConstants.V_7_3;
import static org.springframework.http.HttpStatus.ACCEPTED;
import static org.springframework.http.HttpStatus.OK;

@LaunchAbortArmed
@SpringBootTest
class CertificateControllerIntegrationTest {

    private static final int VALIDITY_MONTHS = 12;
    @Autowired
    @Qualifier("CertificateControllerV73")
    private CertificateController underTest;
    @Autowired
    private VaultService vaultService;

    @SuppressWarnings("checkstyle:MagicNumber")
    public static Stream<Arguments> certificateListProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(HTTPS_LOCALHOST, 5, 0,
                        HTTPS_LOCALHOST + "/certificates?api-version=7.3&$skiptoken=5&maxresults=5&includePending=true"))
                .add(Arguments.of(HTTPS_LOCALHOST_80, 3, 1,
                        HTTPS_LOCALHOST_80 + "/certificates?api-version=7.3&$skiptoken=4&maxresults=3&includePending=true"))
                .add(Arguments.of(HTTPS_DEFAULT_LOWKEY_VAULT, 3, 8,
                        null))
                .build();
    }

    @Test
    void testCreateShouldReturnPendingCertificateWhenCalled() {
        //given
        final CreateCertificateRequest request = getCreateCertificateRequest();

        //when
        final ResponseEntity<KeyVaultPendingCertificateModel> actual = underTest
                .create("create-" + CERT_NAME_1, HTTPS_LOCALHOST_8443, request);

        //then
        Assertions.assertEquals(ACCEPTED, actual.getStatusCode());
        final KeyVaultPendingCertificateModel body = actual.getBody();
        assertPendingCreateResponseIsAsExpected(body);
    }

    @Test
    void testPendingCreateShouldReturnPendingCertificateWhenExists() {
        //given
        final CreateCertificateRequest request = getCreateCertificateRequest();
        underTest.create("pending-" + CERT_NAME_2, HTTPS_LOCALHOST_8443, request);

        //when
        final ResponseEntity<KeyVaultPendingCertificateModel> actual = underTest
                .pendingCreate("pending-" + CERT_NAME_2, HTTPS_LOCALHOST_8443);

        //then
        Assertions.assertEquals(OK, actual.getStatusCode());
        final KeyVaultPendingCertificateModel body = actual.getBody();
        assertPendingCreateResponseIsAsExpected(body);
    }

    @Test
    void testPendingCreateShouldThrowExceptionWhenNotFound() {
        //given

        //when
        Assertions.assertThrows(NotFoundException.class, () -> underTest
                .pendingCreate("pending-" + CERT_NAME_3, HTTPS_LOCALHOST_8443));

        //then + exception
    }

    @Test
    void testGetShouldReturnModelWhenCalledWithValidData() {
        //given
        final CreateCertificateRequest request = getCreateCertificateRequest();
        underTest.create(CERT_NAME_2, HTTPS_LOCALHOST_8443, request);
        final Deque<String> versions = vaultService.findByUri(HTTPS_LOCALHOST_8443)
                .certificateVaultFake()
                .getEntities()
                .getVersions(new CertificateEntityId(HTTPS_LOCALHOST_8443, CERT_NAME_2));

        //when
        final ResponseEntity<KeyVaultCertificateModel> actual = underTest.get(CERT_NAME_2, HTTPS_LOCALHOST_8443);

        //then
        Assertions.assertEquals(OK, actual.getStatusCode());
        final KeyVaultCertificateModel body = actual.getBody();
        Assertions.assertNotNull(body);
        Assertions.assertNotNull(body.getCertificate());
        Assertions.assertNotNull(body.getThumbprint());
        Assertions.assertEquals(Collections.emptyMap(), body.getTags());
        final URI id = new VersionedCertificateEntityId(HTTPS_LOCALHOST_8443, CERT_NAME_2, versions.getFirst())
                .asUri(HTTPS_LOCALHOST_8443);
        Assertions.assertEquals(id.toString(), body.getId());
    }

    @Test
    void testGetWithVersionShouldReturnModelWhenCalledWithValidData() {
        //given
        final CreateCertificateRequest request = getCreateCertificateRequest();
        underTest.create(CERT_NAME_3, HTTPS_LOCALHOST_8443, request);
        final Deque<String> versions = vaultService.findByUri(HTTPS_LOCALHOST_8443)
                .certificateVaultFake()
                .getEntities()
                .getVersions(new CertificateEntityId(HTTPS_LOCALHOST_8443, CERT_NAME_3));

        //when
        final ResponseEntity<KeyVaultCertificateModel> actual = underTest
                .getWithVersion(CERT_NAME_3, versions.getFirst(), HTTPS_LOCALHOST_8443);

        //then
        Assertions.assertEquals(OK, actual.getStatusCode());
        final KeyVaultCertificateModel body = actual.getBody();
        Assertions.assertNotNull(body);
        Assertions.assertNotNull(body.getCertificate());
        Assertions.assertNotNull(body.getThumbprint());
        Assertions.assertEquals(Collections.emptyMap(), body.getTags());
        final URI id = new VersionedCertificateEntityId(HTTPS_LOCALHOST_8443, CERT_NAME_3, versions.getFirst())
                .asUri(HTTPS_LOCALHOST_8443);
        Assertions.assertEquals(id.toString(), body.getId());
    }

    @Test
    void testApiVersionShouldReturnV73WhenCalled() {
        //given

        //when
        final String actual = underTest.apiVersion();

        //then
        Assertions.assertEquals(V_7_3, actual);
    }

    @Test
    void testImportCertificateShouldReturnModelWhenCalledWithValidPemData() {
        //given
        final CertificateImportRequest request = getCreateImportRequest("/cert/ec.pem", CertContentType.PEM);
        final String name = CERT_NAME_3 + "-import-pem";

        //when
        final ResponseEntity<KeyVaultCertificateModel> actual = underTest
                .importCertificate(name, HTTPS_LOCALHOST_8443, request);

        //then
        Assertions.assertEquals(OK, actual.getStatusCode());
        final KeyVaultCertificateModel body = actual.getBody();
        Assertions.assertNotNull(body);
        Assertions.assertNotNull(body.getCertificate());
        Assertions.assertNotNull(body.getThumbprint());
        Assertions.assertEquals(Collections.emptyMap(), body.getTags());
        Assertions.assertTrue(body.getId().startsWith(HTTPS_LOCALHOST_8443.toString()));
        Assertions.assertTrue(body.getId().contains(name));
    }

    @Test
    void testImportCertificateShouldReturnModelWhenCalledWithValidPkcs12Data() {
        //given
        final CertificateImportRequest request = getCreateImportRequest("/cert/ec.p12", CertContentType.PKCS12);
        final String name = CERT_NAME_3 + "-import-pkcs";

        //when
        final ResponseEntity<KeyVaultCertificateModel> actual = underTest
                .importCertificate(name, HTTPS_LOCALHOST_8443, request);

        //then
        Assertions.assertEquals(OK, actual.getStatusCode());
        final KeyVaultCertificateModel body = actual.getBody();
        Assertions.assertNotNull(body);
        Assertions.assertNotNull(body.getCertificate());
        Assertions.assertNotNull(body.getThumbprint());
        Assertions.assertEquals(Collections.emptyMap(), body.getTags());
        Assertions.assertTrue(body.getId().startsWith(HTTPS_LOCALHOST_8443.toString()));
        Assertions.assertTrue(body.getId().contains(name));
    }

    @Test
    void testImportCertificateShouldReturnModelWhenCalledWithValidPemDataAndNoType() {
        //given
        final CertificateImportRequest request = getCreateImportRequest("/cert/ec.pem", null);
        final String name = CERT_NAME_3 + "-import-pem-auto";

        //when
        final ResponseEntity<KeyVaultCertificateModel> actual = underTest
                .importCertificate(name, HTTPS_LOCALHOST_8443, request);

        //then
        Assertions.assertEquals(OK, actual.getStatusCode());
        final KeyVaultCertificateModel body = actual.getBody();
        Assertions.assertNotNull(body);
        Assertions.assertNotNull(body.getCertificate());
        Assertions.assertNotNull(body.getThumbprint());
        Assertions.assertEquals(Collections.emptyMap(), body.getTags());
        Assertions.assertTrue(body.getId().startsWith(HTTPS_LOCALHOST_8443.toString()));
        Assertions.assertTrue(body.getId().contains(name));
    }

    @Test
    void testImportCertificateShouldReturnModelWhenCalledWithValidPkcs12DataAndNoType() {
        //given
        final CertificateImportRequest request = getCreateImportRequest("/cert/ec.p12", null);
        final String name = CERT_NAME_3 + "-import-pkcs-auto";

        //when
        final ResponseEntity<KeyVaultCertificateModel> actual = underTest
                .importCertificate(name, HTTPS_LOCALHOST_8443, request);

        //then
        Assertions.assertEquals(OK, actual.getStatusCode());
        final KeyVaultCertificateModel body = actual.getBody();
        Assertions.assertNotNull(body);
        Assertions.assertNotNull(body.getCertificate());
        Assertions.assertNotNull(body.getThumbprint());
        Assertions.assertEquals(Collections.emptyMap(), body.getTags());
        Assertions.assertTrue(body.getId().startsWith(HTTPS_LOCALHOST_8443.toString()));
        Assertions.assertTrue(body.getId().contains(name));
    }

    @Test
    void testImportCertificateShouldThrowExceptionWhenCalledWithNotMatchingCertTypes() {
        //given
        final CertificateImportRequest request = getCreateImportRequest("/cert/ec.p12", CertContentType.PEM);
        final String name = CERT_NAME_3 + "-import-invalid";

        //when
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> underTest.importCertificate(name, HTTPS_LOCALHOST_8443, request));

        //then + exception
    }

    @Test
    void testVersionsShouldReturnAPageOfVersionsWhenTheCertificateExists() {
        //given
        final CertificateImportRequest request = getCreateImportRequest("/cert/ec.pem", CertContentType.PEM);
        final String name = CERT_NAME_1 + "-versions";
        final KeyVaultCertificateModel imported = Objects
                .requireNonNull(underTest.importCertificate(name, HTTPS_LOCALHOST_8443, request).getBody());

        //when
        final ResponseEntity<KeyVaultItemListModel<KeyVaultCertificateItemModel>> actual = underTest
                .versions(name, HTTPS_LOCALHOST_8443, 1, 0);

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
        vaultService.create(vault);
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

    private CreateCertificateRequest getCreateCertificateRequest() {
        final CreateCertificateRequest request = new CreateCertificateRequest();
        final CertificateKeyModel keyProperties = new CertificateKeyModel();
        keyProperties.setKeyType(KeyType.EC);
        keyProperties.setKeyCurveName(KeyCurveName.P_521);
        keyProperties.setReuseKey(false);
        keyProperties.setExportable(true);

        final CertificateSecretModel secretProperties = new CertificateSecretModel();
        secretProperties.setContentType(CertContentType.PEM.getMimeType());

        final X509CertificateModel x509Properties = new X509CertificateModel();
        x509Properties.setSubject("CN=example.com");
        x509Properties.setValidityMonths(VALIDITY_MONTHS);
        x509Properties.setSubjectAlternativeNames(new SubjectAlternativeNames(Set.of("*.example.com"), Set.of(), Set.of()));

        final CertificatePolicyModel policy = new CertificatePolicyModel();
        policy.setKeyProperties(keyProperties);
        policy.setSecretProperties(secretProperties);
        policy.setX509Properties(x509Properties);
        policy.setIssuer(new IssuerParameterModel(CertAuthorityType.SELF_SIGNED));
        request.setPolicy(policy);

        return request;
    }

    private static void assertPendingCreateResponseIsAsExpected(final KeyVaultPendingCertificateModel body) {
        Assertions.assertNotNull(body);
        Assertions.assertNotNull(body.getCsr());
        Assertions.assertNotNull(body.getRequestId());
        Assertions.assertNull(body.getStatusDetails());
        Assertions.assertEquals("completed", body.getStatus());
    }
}
