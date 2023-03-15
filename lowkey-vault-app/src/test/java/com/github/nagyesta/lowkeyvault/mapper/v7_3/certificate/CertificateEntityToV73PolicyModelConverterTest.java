package com.github.nagyesta.lowkeyvault.mapper.v7_3.certificate;

import com.github.nagyesta.lowkeyvault.model.v7_3.certificate.CertificatePolicyModel;
import com.github.nagyesta.lowkeyvault.service.certificate.ReadOnlyKeyVaultCertificateEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CertificateEntityToV73PolicyModelConverterTest {

    @SuppressWarnings("ConstantConditions")
    @Test
    void testConstructorShouldThrowExceptionWhenCalledWithNullModelSupplier() {
        //given

        //when
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new BaseCertificateEntityToV73PolicyModelConverter(null,
                        ReadOnlyKeyVaultCertificateEntity::getOriginalCertificatePolicy));

        //then + exceptions
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void testConstructorShouldThrowExceptionWhenCalledWithNullPolicyExtractor() {
        //given

        //when
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new BaseCertificateEntityToV73PolicyModelConverter(CertificatePolicyModel::new, null));

        //then + exceptions
    }
}
