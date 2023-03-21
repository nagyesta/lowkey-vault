package com.github.nagyesta.lowkeyvault.mapper.v7_3.certificate;

import com.github.nagyesta.lowkeyvault.model.v7_3.certificate.CertificatePolicyModel;
import com.github.nagyesta.lowkeyvault.service.certificate.ReadOnlyKeyVaultCertificateEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CertificateEntityToV73PolicyModelConverterTest {

    @SuppressWarnings("DataFlowIssue")
    @Test
    void testConstructorShouldThrowExceptionWhenCalledWithNull() {
        //given

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> new CertificateEntityToV73PolicyModelConverter(null));

        //then + exception
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void testConstructorShouldThrowExceptionWhenCalledWithNullModelSupplier() {
        //given

        //when
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new BaseCertificateEntityToV73PolicyModelConverter(null,
                        ReadOnlyKeyVaultCertificateEntity::getOriginalCertificatePolicy) {
                    @Override
                    public void afterPropertiesSet() {
                    }
                });

        //then + exceptions
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void testConstructorShouldThrowExceptionWhenCalledWithNullPolicyExtractor() {
        //given

        //when
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new BaseCertificateEntityToV73PolicyModelConverter(CertificatePolicyModel::new, null) {
                    @Override
                    public void afterPropertiesSet() {
                    }
                });

        //then + exceptions
    }
}
