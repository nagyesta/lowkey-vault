package com.github.nagyesta.lowkeyvault.controller.v7_3;

import com.github.nagyesta.lowkeyvault.model.v7_2.common.constants.RecoveryLevel;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyCurveName;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyType;
import com.github.nagyesta.lowkeyvault.model.v7_3.certificate.*;
import com.github.nagyesta.lowkeyvault.service.certificate.impl.CertAuthorityType;
import com.github.nagyesta.lowkeyvault.service.certificate.impl.CertContentType;
import com.github.nagyesta.lowkeyvault.service.vault.VaultFake;
import com.github.nagyesta.lowkeyvault.service.vault.VaultService;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.URI;
import java.util.Set;

import static com.github.nagyesta.lowkeyvault.TestConstantsCertificates.VALIDITY_MONTHS_ONE_YEAR;

public abstract class BaseCertificateControllerIntegrationTest {

    @Autowired
    private VaultService vaultService;

    protected VaultFake findByUri(final URI baseUri) {
        return vaultService.findByUri(baseUri);
    }

    protected VaultFake create(final URI baseUri) {
        return vaultService.create(baseUri);
    }

    protected void prepareVaultIfNotExists(final URI baseUri) {
        if (vaultService.list().stream().map(VaultFake::baseUri).noneMatch(baseUri::equals)) {
            vaultService.create(baseUri, RecoveryLevel.RECOVERABLE_AND_PURGEABLE, RecoveryLevel.MAX_RECOVERABLE_DAYS_INCLUSIVE, Set.of());
        }
    }

    protected CreateCertificateRequest getCreateCertificateRequest() {
        final var request = new CreateCertificateRequest();
        final var keyProperties = new CertificateKeyModel();
        keyProperties.setKeyType(KeyType.EC);
        keyProperties.setKeyCurveName(KeyCurveName.P_521);
        keyProperties.setReuseKey(false);
        keyProperties.setExportable(true);

        final var secretProperties = new CertificateSecretModel();
        secretProperties.setContentType(CertContentType.PEM.getMimeType());

        final var x509Properties = new X509CertificateModel();
        x509Properties.setSubject("CN=example.com");
        x509Properties.setValidityMonths(VALIDITY_MONTHS_ONE_YEAR);
        x509Properties.setSubjectAlternativeNames(new SubjectAlternativeNames(Set.of("*.example.com"), Set.of(), Set.of()));

        final var policy = new CertificatePolicyModel();
        policy.setKeyProperties(keyProperties);
        policy.setSecretProperties(secretProperties);
        policy.setX509Properties(x509Properties);
        policy.setIssuer(new IssuerParameterModel(CertAuthorityType.SELF_SIGNED));
        request.setPolicy(policy);

        return request;
    }

    protected void assertPendingCreateResponseIsAsExpected(final KeyVaultPendingCertificateModel body) {
        Assertions.assertNotNull(body);
        Assertions.assertNotNull(body.getCsr());
        Assertions.assertNotNull(body.getRequestId());
        Assertions.assertNull(body.getStatusDetails());
        Assertions.assertEquals("completed", body.getStatus());
    }
}
