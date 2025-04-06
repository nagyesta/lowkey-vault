package com.github.nagyesta.lowkeyvault.context;

import com.github.nagyesta.lowkeyvault.mapper.common.registry.CertificateConverterRegistry;
import com.github.nagyesta.lowkeyvault.mapper.v7_3.certificate.*;
import com.github.nagyesta.lowkeyvault.service.vault.VaultService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

@Configuration
public class CertificateConverterConfiguration {

    private final VaultService vaultService;

    public CertificateConverterConfiguration(final VaultService vaultService) {
        this.vaultService = vaultService;
    }

    @Bean
    public CertificateConverterRegistry certificateConverterRegistry() {
        return new CertificateConverterRegistry();
    }

    @Bean
    public CertificateEntityToV73PropertiesModelConverter certificatePropertiesConverter() {
        return new CertificateEntityToV73PropertiesModelConverter(certificateConverterRegistry());
    }

    @Bean
    @DependsOn("certificatePropertiesConverter")
    public CertificateEntityToV73ModelConverter certificateModelConverter() {
        return new CertificateEntityToV73ModelConverter(certificateConverterRegistry());
    }

    @Bean
    @DependsOn("certificatePropertiesConverter")
    public CertificateEntityToV73CertificateItemModelConverter certificateItemConverter() {
        return new CertificateEntityToV73CertificateItemModelConverter(certificateConverterRegistry());
    }

    @Bean
    @DependsOn("certificatePropertiesConverter")
    public CertificateEntityToV73CertificateVersionItemModelConverter certificateVersionedItemConverter() {
        return new CertificateEntityToV73CertificateVersionItemModelConverter(certificateConverterRegistry());
    }

    @Bean
    public CertificateEntityToV73PolicyModelConverter certificatePolicyConverter() {
        return new CertificateEntityToV73PolicyModelConverter(certificateConverterRegistry());
    }

    @Bean
    public CertificateEntityToV73IssuancePolicyModelConverter certificateIssuancePolicyConverter() {
        return new CertificateEntityToV73IssuancePolicyModelConverter(certificateConverterRegistry());
    }

    @Bean
    public CertificateEntityToV73PendingCertificateOperationModelConverter certificatePendingOperationConverter() {
        return new CertificateEntityToV73PendingCertificateOperationModelConverter(certificateConverterRegistry());
    }

    @Bean
    public CertificateLifetimeActionsPolicyToV73ModelConverter certificateLifetimeActionConverter() {
        return new CertificateLifetimeActionsPolicyToV73ModelConverter(certificateConverterRegistry());
    }

    @Bean
    @DependsOn({
            "certificatePropertiesConverter",
            "certificatePolicyConverter",
            "certificateIssuancePolicyConverter",
            "certificateLifetimeActionConverter"})
    public CertificateEntityToV73BackupConverter certificateBackupConverter() {
        return new CertificateEntityToV73BackupConverter(certificateConverterRegistry(), vaultService);
    }
}
