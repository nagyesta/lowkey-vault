package com.github.nagyesta.lowkeyvault.controller.v7_3;

import com.github.nagyesta.abortmission.booster.jupiter.annotation.LaunchAbortArmed;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.util.Collections;
import java.util.Deque;
import java.util.Set;

import static com.github.nagyesta.lowkeyvault.TestConstantsCertificates.*;
import static com.github.nagyesta.lowkeyvault.TestConstantsUri.HTTPS_LOCALHOST_8443;
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
