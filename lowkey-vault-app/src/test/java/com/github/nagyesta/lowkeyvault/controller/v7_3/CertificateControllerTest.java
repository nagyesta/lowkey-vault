package com.github.nagyesta.lowkeyvault.controller.v7_3;

import com.github.nagyesta.lowkeyvault.mapper.v7_3.certificate.CertificateEntityToV73CertificateItemModelConverter;
import com.github.nagyesta.lowkeyvault.mapper.v7_3.certificate.CertificateEntityToV73CertificateVersionItemModelConverter;
import com.github.nagyesta.lowkeyvault.mapper.v7_3.certificate.CertificateEntityToV73ModelConverter;
import com.github.nagyesta.lowkeyvault.mapper.v7_3.certificate.CertificateEntityToV73PendingCertificateOperationModelConverter;
import com.github.nagyesta.lowkeyvault.service.vault.VaultService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.mockito.Mockito.mock;

class CertificateControllerTest {

    public static Stream<Arguments> nullProvider() {
        final CertificateEntityToV73ModelConverter modelConverter =
                mock(CertificateEntityToV73ModelConverter.class);
        final CertificateEntityToV73CertificateItemModelConverter itemModelConverter =
                mock(CertificateEntityToV73CertificateItemModelConverter.class);
        final CertificateEntityToV73CertificateVersionItemModelConverter versionItemModelConverter =
                mock(CertificateEntityToV73CertificateVersionItemModelConverter.class);
        final CertificateEntityToV73PendingCertificateOperationModelConverter pendingModelConverter =
                mock(CertificateEntityToV73PendingCertificateOperationModelConverter.class);
        final VaultService vaultService = mock(VaultService.class);
        return Stream.<Arguments>builder()
                .add(Arguments.of(null, null, null, null, null))
                .add(Arguments.of(modelConverter, null, null, null, null))
                .add(Arguments.of(null, itemModelConverter, null, null, null))
                .add(Arguments.of(null, null, versionItemModelConverter, null, null))
                .add(Arguments.of(null, null, null, pendingModelConverter, null))
                .add(Arguments.of(null, null, null, null, vaultService))
                .add(Arguments.of(null, itemModelConverter, versionItemModelConverter, pendingModelConverter, vaultService))
                .add(Arguments.of(modelConverter, null, versionItemModelConverter, pendingModelConverter, vaultService))
                .add(Arguments.of(modelConverter, itemModelConverter, null, pendingModelConverter, vaultService))
                .add(Arguments.of(modelConverter, itemModelConverter, versionItemModelConverter, null, vaultService))
                .add(Arguments.of(modelConverter, itemModelConverter, versionItemModelConverter, pendingModelConverter, null))
                .build();
    }

    @ParameterizedTest
    @MethodSource("nullProvider")
    void testConstructorShouldThrowExceptionWhenCalledWithNulls(
            final CertificateEntityToV73ModelConverter modelConverter,
            final CertificateEntityToV73CertificateItemModelConverter itemModelConverter,
            final CertificateEntityToV73CertificateVersionItemModelConverter versionItemModelConverter,
            final CertificateEntityToV73PendingCertificateOperationModelConverter pendingModelConverter,
            final VaultService vaultService) {
        //given

        //when
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new CertificateController(
                        modelConverter, itemModelConverter, versionItemModelConverter, pendingModelConverter, vaultService));

        //then + exception
    }
}
