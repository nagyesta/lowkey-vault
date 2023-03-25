package com.github.nagyesta.lowkeyvault.controller.v7_3;

import com.github.nagyesta.abortmission.booster.jupiter.annotation.LaunchAbortArmed;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyType;
import com.github.nagyesta.lowkeyvault.model.v7_3.certificate.*;
import com.github.nagyesta.lowkeyvault.service.certificate.id.CertificateEntityId;
import com.github.nagyesta.lowkeyvault.service.certificate.id.VersionedCertificateEntityId;
import com.github.nagyesta.lowkeyvault.service.certificate.impl.CertAuthorityType;
import com.github.nagyesta.lowkeyvault.service.certificate.impl.CertContentType;
import com.github.nagyesta.lowkeyvault.service.exception.NotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.util.Deque;
import java.util.Set;

import static com.github.nagyesta.lowkeyvault.TestConstantsCertificates.CERT_NAME_2;
import static com.github.nagyesta.lowkeyvault.TestConstantsCertificates.CERT_NAME_3;
import static com.github.nagyesta.lowkeyvault.TestConstantsUri.HTTPS_LOOP_BACK_IP_8443;
import static com.github.nagyesta.lowkeyvault.model.common.ApiConstants.V_7_3;
import static org.springframework.http.HttpStatus.OK;

@LaunchAbortArmed
@SpringBootTest
class CertificatePolicyControllerIntegrationTest extends BaseCertificateControllerIntegrationTest {

    @Autowired
    @Qualifier("CertificatePolicyControllerV73")
    private CertificatePolicyController underTest;
    @Autowired
    @Qualifier("CertificateControllerV73")
    private CertificateController certificateController;

    @BeforeEach
    void setUp() {
        prepareVaultIfNotExists(HTTPS_LOOP_BACK_IP_8443);
    }

    @Test
    void testPendingCreateShouldReturnPendingCertificateWhenExists() {
        //given
        final CreateCertificateRequest request = getCreateCertificateRequest();
        certificateController.create("pending-" + CERT_NAME_2, HTTPS_LOOP_BACK_IP_8443, request);

        //when
        final ResponseEntity<KeyVaultPendingCertificateModel> actual = underTest
                .pendingCreate("pending-" + CERT_NAME_2, HTTPS_LOOP_BACK_IP_8443);

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
                .pendingCreate("pending-" + CERT_NAME_3, HTTPS_LOOP_BACK_IP_8443));

        //then + exception
    }

    @Test
    void testPendingDeleteShouldReturnPendingCertificateWhenExists() {
        //given
        final CreateCertificateRequest request = getCreateCertificateRequest();
        final String certificateName = "pending-del-" + CERT_NAME_2;
        certificateController.create(certificateName, HTTPS_LOOP_BACK_IP_8443, request);
        certificateController.delete(certificateName, HTTPS_LOOP_BACK_IP_8443);

        //when
        final ResponseEntity<KeyVaultPendingCertificateModel> actual = underTest
                .pendingDelete(certificateName, HTTPS_LOOP_BACK_IP_8443);

        //then
        Assertions.assertEquals(OK, actual.getStatusCode());
        final KeyVaultPendingCertificateModel body = actual.getBody();
        assertPendingCreateResponseIsAsExpected(body);
    }

    @Test
    void testPendingDeleteShouldThrowExceptionWhenNotFound() {
        //given

        //when
        Assertions.assertThrows(NotFoundException.class, () -> underTest
                .pendingDelete("pending-del-" + CERT_NAME_3, HTTPS_LOOP_BACK_IP_8443));

        //then + exception
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
    void testGetPolicyShouldReturnModelWhenCalledWithValidData() {
        //given
        final CreateCertificateRequest request = getCreateCertificateRequest();
        final String certificateName = CERT_NAME_2 + "policy";
        certificateController.create(certificateName, HTTPS_LOOP_BACK_IP_8443, request);
        final Deque<String> versions = findByUri(HTTPS_LOOP_BACK_IP_8443)
                .certificateVaultFake()
                .getEntities()
                .getVersions(new CertificateEntityId(HTTPS_LOOP_BACK_IP_8443, certificateName));

        //when
        final ResponseEntity<CertificatePolicyModel> actual = underTest.getPolicy(certificateName, HTTPS_LOOP_BACK_IP_8443);

        //then
        Assertions.assertEquals(OK, actual.getStatusCode());
        final CertificatePolicyModel body = actual.getBody();
        Assertions.assertNotNull(body);
        final URI id = new VersionedCertificateEntityId(HTTPS_LOOP_BACK_IP_8443, certificateName, versions.getFirst())
                .asPolicyUri(HTTPS_LOOP_BACK_IP_8443);
        Assertions.assertEquals(request.getPolicy().getSecretProperties(), body.getSecretProperties());
        Assertions.assertEquals(request.getPolicy().getKeyProperties(), body.getKeyProperties());
        Assertions.assertEquals(request.getPolicy().getX509Properties(), body.getX509Properties());
        Assertions.assertTrue(body.getAttributes().isEnabled());
        Assertions.assertNull(body.getAttributes().getRecoveryLevel());
        Assertions.assertEquals(id.toString(), body.getId());
    }

    @Test
    void testUpdatePolicyShouldReturnModelWhenCalledWithValidData() {
        //given
        final CreateCertificateRequest request = getCreateCertificateRequest();
        final String certificateName = CERT_NAME_2 + "-update-policy";
        certificateController.create(certificateName, HTTPS_LOOP_BACK_IP_8443, request);
        final Deque<String> versions = findByUri(HTTPS_LOOP_BACK_IP_8443)
                .certificateVaultFake()
                .getEntities()
                .getVersions(new CertificateEntityId(HTTPS_LOOP_BACK_IP_8443, certificateName));
        final CertificatePolicyModel update = getUpdatePolicyRequest();

        //when
        final ResponseEntity<CertificatePolicyModel> actual = underTest.updatePolicy(certificateName, HTTPS_LOOP_BACK_IP_8443, update);

        //then
        Assertions.assertEquals(OK, actual.getStatusCode());
        final CertificatePolicyModel body = actual.getBody();
        Assertions.assertNotNull(body);
        final URI id = new VersionedCertificateEntityId(HTTPS_LOOP_BACK_IP_8443, certificateName, versions.getFirst())
                .asPolicyUri(HTTPS_LOOP_BACK_IP_8443);
        Assertions.assertEquals(update.getSecretProperties(), body.getSecretProperties());
        Assertions.assertEquals(update.getKeyProperties(), body.getKeyProperties());
        Assertions.assertEquals(update.getX509Properties(), body.getX509Properties());
        Assertions.assertTrue(body.getAttributes().isEnabled());
        Assertions.assertNull(body.getAttributes().getRecoveryLevel());
        Assertions.assertEquals(id.toString(), body.getId());
    }

    private static CertificatePolicyModel getUpdatePolicyRequest() {
        final CertificateKeyModel keyProperties = new CertificateKeyModel();
        keyProperties.setKeyType(KeyType.RSA);
        keyProperties.setKeySize(KeyType.RSA.validateOrDefault(null, Integer.class));
        keyProperties.setReuseKey(false);
        keyProperties.setExportable(true);

        final CertificateSecretModel secretProperties = new CertificateSecretModel();
        secretProperties.setContentType(CertContentType.PEM.getMimeType());

        final X509CertificateModel x509Properties = new X509CertificateModel();
        x509Properties.setSubject("CN=localhost");
        x509Properties.setValidityMonths(1);
        x509Properties.setSubjectAlternativeNames(new SubjectAlternativeNames(Set.of("example.com", "*.example.com"), Set.of(), Set.of()));
        x509Properties.setKeyUsage(Set.of());
        x509Properties.setExtendedKeyUsage(Set.of());

        final CertificatePolicyModel policy = new CertificatePolicyModel();
        policy.setKeyProperties(keyProperties);
        policy.setSecretProperties(secretProperties);
        policy.setX509Properties(x509Properties);
        policy.setIssuer(new IssuerParameterModel(CertAuthorityType.SELF_SIGNED));
        return policy;
    }
}
