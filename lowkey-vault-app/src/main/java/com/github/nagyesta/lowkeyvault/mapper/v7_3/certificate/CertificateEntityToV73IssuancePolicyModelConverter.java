package com.github.nagyesta.lowkeyvault.mapper.v7_3.certificate;

import com.github.nagyesta.lowkeyvault.mapper.common.AliasAwareConverter;
import com.github.nagyesta.lowkeyvault.model.v7_3.certificate.CertificatePolicyModel;
import com.github.nagyesta.lowkeyvault.service.certificate.ReadOnlyKeyVaultCertificateEntity;
import org.jspecify.annotations.Nullable;
import org.mapstruct.*;

import java.net.URI;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {
                CertificateEntityToV73PropertiesModelConverter.class,
                BaseCertificateEntityToV73PolicyModelConverter.class
        }
)
public interface CertificateEntityToV73IssuancePolicyModelConverter
        extends AliasAwareConverter<ReadOnlyKeyVaultCertificateEntity, CertificatePolicyModel> {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "attributes", source = "source")
    @Mapping(target = "issuer", source = "source.issuancePolicy", nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
    @Mapping(target = "keyProperties", source = "source.issuancePolicy", nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
    @Mapping(target = "secretProperties", source = "source.issuancePolicy", nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
    @Mapping(target = "x509Properties", source = "source.issuancePolicy", nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
    @Mapping(target = "lifetimeActions", ignore = true)
    @Override
    @Nullable CertificatePolicyModel convert(@Nullable ReadOnlyKeyVaultCertificateEntity source, URI vaultUri);

    @AfterMapping
    default void postProcess(
            @Nullable final ReadOnlyKeyVaultCertificateEntity source,
            final URI vaultUri,
            @Nullable @MappingTarget final CertificatePolicyModel model) {
        if (source != null && model != null) {
            model.setId(source.getId().asPolicyUri(vaultUri).toString());
        }
    }
}
