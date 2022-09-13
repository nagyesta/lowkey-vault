package com.github.nagyesta.lowkeyvault.service.certificate.impl;

import com.github.nagyesta.lowkeyvault.model.v7_2.common.constants.RecoveryLevel;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyCurveName;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyType;
import com.github.nagyesta.lowkeyvault.service.certificate.CertificateVaultFake;
import com.github.nagyesta.lowkeyvault.service.certificate.ReadOnlyKeyVaultCertificateEntity;
import com.github.nagyesta.lowkeyvault.service.certificate.id.VersionedCertificateEntityId;
import com.github.nagyesta.lowkeyvault.service.vault.VaultFake;
import com.github.nagyesta.lowkeyvault.service.vault.impl.VaultFakeImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static com.github.nagyesta.lowkeyvault.TestConstants.*;
import static com.github.nagyesta.lowkeyvault.TestConstantsCertificates.*;
import static com.github.nagyesta.lowkeyvault.TestConstantsUri.HTTPS_LOCALHOST_8443;
import static com.github.nagyesta.lowkeyvault.service.certificate.impl.CertAuthorityType.UNKNOWN;
import static com.github.nagyesta.lowkeyvault.service.certificate.impl.KeyVaultCertificateEntityTest.VALIDITY_MONTHS;
import static org.mockito.Mockito.*;

class CertificateVaultFakeImplTest {

    @SuppressWarnings("ConstantConditions")
    @Test
    void testConstructorShouldThrowExceptionWhenCalledWithNull() {
        //given

        //when
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new CertificateVaultFakeImpl(null, null, null));

        //then + exception
    }

    @Test
    void testCreateVersionedIdShouldReturnVersionedIdWhenCalledWithValidInput() {
        //given
        final VaultFake vault = mock(VaultFake.class);
        when(vault.baseUri()).thenReturn(HTTPS_LOCALHOST_8443);
        final CertificateVaultFakeImpl underTest = new CertificateVaultFakeImpl(vault, RecoveryLevel.PURGEABLE, null);

        //when
        final VersionedCertificateEntityId actual = underTest.createVersionedId(CERT_NAME_1, CERT_VERSION_1);

        //then
        Assertions.assertEquals(VERSIONED_CERT_ENTITY_ID_1_VERSION_1, actual);
        verify(vault).baseUri();
    }

    @Test
    void testCreateCertificateVersionShouldGenerateCertificateAndCsrWhenCalledWithValidInput() {
        //given
        final CertificateCreationInput input = CertificateCreationInput.builder()
                .validityStart(NOW)
                .subject("CN=" + LOCALHOST)
                .ips(Set.of(LOOP_BACK_IP))
                .name(CERT_NAME_1)
                .dnsNames(Set.of(LOWKEY_VAULT))
                .enableTransparency(false)
                .certAuthorityType(UNKNOWN)
                .contentType(CertContentType.PEM)
                .certificateType(null)
                .keyType(KeyType.EC)
                .keyCurveName(KeyCurveName.P_521)
                .extendedKeyUsage(Set.of("1.3.6.1.5.5.7.3.1", "1.3.6.1.5.5.7.3.2"))
                .keyUsage(Set.of(KeyUsageEnum.KEY_ENCIPHERMENT))
                .reuseKeyOnRenewal(true)
                .validityMonths(VALIDITY_MONTHS)
                .exportablePrivateKey(true)
                .build();

        final VaultFake vault = new VaultFakeImpl(HTTPS_LOCALHOST_8443);
        final CertificateVaultFake underTest = vault.certificateVaultFake();

        //when
        final VersionedCertificateEntityId entityId = underTest.createCertificateVersion(CERT_NAME_1, input);

        //then
        final ReadOnlyKeyVaultCertificateEntity actual = underTest.getEntities().getReadOnlyEntity(entityId);
        Assertions.assertNotNull(actual.getCertificate());
        Assertions.assertNotNull(actual.getCertificateSigningRequest());
    }
}
