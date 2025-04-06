package com.github.nagyesta.lowkeyvault.controller.v7_5;

import com.github.nagyesta.abortmission.booster.jupiter.annotation.LaunchAbortArmed;
import com.github.nagyesta.lowkeyvault.controller.v7_3.BaseCertificateControllerIntegrationTest;
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

import java.net.URI;
import java.util.Set;

import static com.github.nagyesta.lowkeyvault.TestConstantsCertificates.CERT_NAME_2;
import static com.github.nagyesta.lowkeyvault.TestConstantsCertificates.CERT_NAME_3;
import static com.github.nagyesta.lowkeyvault.TestConstantsUri.getRandomVaultUri;
import static com.github.nagyesta.lowkeyvault.model.common.ApiConstants.V_7_5;
import static com.github.nagyesta.lowkeyvault.service.certificate.impl.CertificateCreationInput.DEFAULT_EC_KEY_USAGES;
import static com.github.nagyesta.lowkeyvault.service.certificate.impl.CertificateCreationInput.DEFAULT_EXT_KEY_USAGES;
import static org.springframework.http.HttpStatus.OK;

@LaunchAbortArmed
@SpringBootTest
class CertificatePolicyControllerIntegrationTest extends BaseCertificateControllerIntegrationTest {

    private static final URI VAULT_URI_1 = getRandomVaultUri();
    @Autowired
    @Qualifier("certificatePolicyControllerV75")
    private CertificatePolicyController underTest;
    @Autowired
    @Qualifier("certificateControllerV75")
    private CertificateController certificateController;

    @BeforeEach
    void setUp() {
        prepareVaultIfNotExists(VAULT_URI_1);
    }

    @Test
    void testPendingCreateShouldReturnPendingCertificateWhenExists() {
        //given
        final var request = getCreateCertificateRequest();
        certificateController.create("pending-" + CERT_NAME_2, VAULT_URI_1, request);

        //when
        final var actual = underTest
                .pendingCreate("pending-" + CERT_NAME_2, VAULT_URI_1);

        //then
        Assertions.assertEquals(OK, actual.getStatusCode());
        final var body = actual.getBody();
        assertPendingCreateResponseIsAsExpected(body);
    }

    @Test
    void testPendingCreateShouldThrowExceptionWhenNotFound() {
        //given

        //when
        Assertions.assertThrows(NotFoundException.class, () -> underTest
                .pendingCreate("pending-" + CERT_NAME_3, VAULT_URI_1));

        //then + exception
    }

    @Test
    void testPendingDeleteShouldReturnPendingCertificateWhenExists() {
        //given
        final var request = getCreateCertificateRequest();
        final var certificateName = "pending-del-" + CERT_NAME_2;
        certificateController.create(certificateName, VAULT_URI_1, request);
        certificateController.delete(certificateName, VAULT_URI_1);

        //when
        final var actual = underTest
                .pendingDelete(certificateName, VAULT_URI_1);

        //then
        Assertions.assertEquals(OK, actual.getStatusCode());
        final var body = actual.getBody();
        assertPendingCreateResponseIsAsExpected(body);
    }

    @Test
    void testPendingDeleteShouldThrowExceptionWhenNotFound() {
        //given

        //when
        Assertions.assertThrows(NotFoundException.class, () -> underTest
                .pendingDelete("pending-del-" + CERT_NAME_3, VAULT_URI_1));

        //then + exception
    }

    @Test
    void testApiVersionShouldReturnV75WhenCalled() {
        //given

        //when
        final var actual = underTest.apiVersion();

        //then
        Assertions.assertEquals(V_7_5, actual);
    }

    @Test
    void testGetPolicyShouldReturnModelWhenCalledWithValidData() {
        //given
        final var request = getCreateCertificateRequest();
        final var certificateName = CERT_NAME_2 + "policy";
        certificateController.create(certificateName, VAULT_URI_1, request);
        final var versions = findByUri(VAULT_URI_1)
                .certificateVaultFake()
                .getEntities()
                .getVersions(new CertificateEntityId(VAULT_URI_1, certificateName));

        //when
        final var actual = underTest.getPolicy(certificateName, VAULT_URI_1);

        //then
        Assertions.assertEquals(OK, actual.getStatusCode());
        final var body = actual.getBody();
        Assertions.assertNotNull(body);
        final var id = new VersionedCertificateEntityId(VAULT_URI_1, certificateName, versions.getFirst())
                .asPolicyUri(VAULT_URI_1);
        Assertions.assertEquals(request.getPolicy().getSecretProperties(), body.getSecretProperties());
        Assertions.assertEquals(request.getPolicy().getKeyProperties(), body.getKeyProperties());
        final var x509Properties = request.getPolicy().getX509Properties();
        x509Properties.setExtendedKeyUsage(DEFAULT_EXT_KEY_USAGES);
        x509Properties.setKeyUsage(DEFAULT_EC_KEY_USAGES);
        Assertions.assertEquals(x509Properties, body.getX509Properties());
        Assertions.assertTrue(body.getAttributes().isEnabled());
        Assertions.assertNull(body.getAttributes().getRecoveryLevel());
        Assertions.assertEquals(id.toString(), body.getId());
    }

    @Test
    void testUpdatePolicyShouldReturnModelWhenCalledWithValidData() {
        //given
        final var request = getCreateCertificateRequest();
        final var certificateName = CERT_NAME_2 + "-update-policy";
        certificateController.create(certificateName, VAULT_URI_1, request);
        final var versions = findByUri(VAULT_URI_1)
                .certificateVaultFake()
                .getEntities()
                .getVersions(new CertificateEntityId(VAULT_URI_1, certificateName));
        final var update = getUpdatePolicyRequest();

        //when
        final var actual = underTest.updatePolicy(certificateName, VAULT_URI_1, update);

        //then
        Assertions.assertEquals(OK, actual.getStatusCode());
        final var body = actual.getBody();
        Assertions.assertNotNull(body);
        final var id = new VersionedCertificateEntityId(VAULT_URI_1, certificateName, versions.getFirst())
                .asPolicyUri(VAULT_URI_1);
        Assertions.assertEquals(update.getSecretProperties(), body.getSecretProperties());
        Assertions.assertEquals(update.getKeyProperties(), body.getKeyProperties());
        Assertions.assertEquals(update.getX509Properties(), body.getX509Properties());
        Assertions.assertTrue(body.getAttributes().isEnabled());
        Assertions.assertNull(body.getAttributes().getRecoveryLevel());
        Assertions.assertEquals(id.toString(), body.getId());
    }

    private static CertificatePolicyModel getUpdatePolicyRequest() {
        final var keyProperties = new CertificateKeyModel();
        keyProperties.setKeyType(KeyType.RSA);
        keyProperties.setKeySize(KeyType.RSA.validateOrDefault(null, Integer.class));
        keyProperties.setReuseKey(false);
        keyProperties.setExportable(true);

        final var secretProperties = new CertificateSecretModel();
        secretProperties.setContentType(CertContentType.PEM.getMimeType());

        final var x509Properties = new X509CertificateModel();
        x509Properties.setSubject("CN=localhost");
        x509Properties.setValidityMonths(1);
        x509Properties.setSubjectAlternativeNames(new SubjectAlternativeNames(Set.of("example.com", "*.example.com"), Set.of(), Set.of()));
        x509Properties.setKeyUsage(Set.of());
        x509Properties.setExtendedKeyUsage(Set.of());

        final var policy = new CertificatePolicyModel();
        policy.setKeyProperties(keyProperties);
        policy.setSecretProperties(secretProperties);
        policy.setX509Properties(x509Properties);
        policy.setIssuer(new IssuerParameterModel(CertAuthorityType.SELF_SIGNED));
        return policy;
    }
}
