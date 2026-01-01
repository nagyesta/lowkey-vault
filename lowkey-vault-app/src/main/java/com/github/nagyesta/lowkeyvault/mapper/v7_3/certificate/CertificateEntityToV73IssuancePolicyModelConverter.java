package com.github.nagyesta.lowkeyvault.mapper.v7_3.certificate;

import com.github.nagyesta.lowkeyvault.mapper.common.AliasAwareConverter;
import com.github.nagyesta.lowkeyvault.model.v7_3.certificate.CertificatePolicyModel;
import com.github.nagyesta.lowkeyvault.service.certificate.ReadOnlyKeyVaultCertificateEntity;
import org.jspecify.annotations.Nullable;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

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



    @Override
    default @Nullable CertificatePolicyModel convert(
            final @Nullable ReadOnlyKeyVaultCertificateEntity source,
            final URI vaultUri) {
        if (source == null) {
            return null;
        }
        return doConvert(source, vaultUri);
    }

    @Mapping(target = "id", expression = "java(source.getId().asPolicyUri(vaultUri).toString())")
    @Mapping(target = "attributes", source = "source")
    @Mapping(target = "issuer", source = "source.issuancePolicy")
    @Mapping(target = "keyProperties", source = "source.issuancePolicy")
    @Mapping(target = "secretProperties", source = "source.issuancePolicy")
    @Mapping(target = "x509Properties", source = "source.issuancePolicy")
    @Mapping(target = "lifetimeActions", ignore = true)
    CertificatePolicyModel doConvert(ReadOnlyKeyVaultCertificateEntity source, URI vaultUri);

}
