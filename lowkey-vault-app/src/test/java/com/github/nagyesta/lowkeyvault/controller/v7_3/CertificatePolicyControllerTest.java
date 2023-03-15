package com.github.nagyesta.lowkeyvault.controller.v7_3;

import com.github.nagyesta.lowkeyvault.mapper.v7_3.certificate.CertificateEntityToV73IssuancePolicyModelConverter;
import com.github.nagyesta.lowkeyvault.mapper.v7_3.certificate.CertificateEntityToV73PendingCertificateOperationModelConverter;
import com.github.nagyesta.lowkeyvault.mapper.v7_3.certificate.LifetimeActionsPolicyToV73ModelConverter;
import com.github.nagyesta.lowkeyvault.service.vault.VaultService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.mockito.Mockito.mock;

class CertificatePolicyControllerTest {

    public static Stream<Arguments> nullProvider() {
        final CertificateEntityToV73PendingCertificateOperationModelConverter pendingOperationConverter =
                mock(CertificateEntityToV73PendingCertificateOperationModelConverter.class);
        final CertificateEntityToV73IssuancePolicyModelConverter issuancePolicyConverter =
                mock(CertificateEntityToV73IssuancePolicyModelConverter.class);
        final LifetimeActionsPolicyToV73ModelConverter lifetimeActionsConverter =
                mock(LifetimeActionsPolicyToV73ModelConverter.class);
        final VaultService vaultService = mock(VaultService.class);
        return Stream.<Arguments>builder()
                .add(Arguments.of(null, null, null, null))
                .add(Arguments.of(pendingOperationConverter, null, null, null))
                .add(Arguments.of(null, issuancePolicyConverter, null, null))
                .add(Arguments.of(null, null, lifetimeActionsConverter, null))
                .add(Arguments.of(null, null, null, vaultService))
                .add(Arguments.of(null, issuancePolicyConverter, lifetimeActionsConverter, vaultService))
                .add(Arguments.of(pendingOperationConverter, null, lifetimeActionsConverter, vaultService))
                .add(Arguments.of(pendingOperationConverter, issuancePolicyConverter, null, vaultService))
                .add(Arguments.of(pendingOperationConverter, issuancePolicyConverter, lifetimeActionsConverter, null))
                .build();
    }

    @ParameterizedTest
    @MethodSource("nullProvider")
    void testConstructorShouldThrowExceptionWhenCalledWithNull(
            final CertificateEntityToV73PendingCertificateOperationModelConverter pendingOperationConverter,
            final CertificateEntityToV73IssuancePolicyModelConverter issuancePolicyConverter,
            final LifetimeActionsPolicyToV73ModelConverter lifetimeActionsConverter,
            final VaultService vaultService) {
        //given

        //when
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new CertificatePolicyController(pendingOperationConverter, issuancePolicyConverter,
                        lifetimeActionsConverter, vaultService));

        //then + exception
    }
}
