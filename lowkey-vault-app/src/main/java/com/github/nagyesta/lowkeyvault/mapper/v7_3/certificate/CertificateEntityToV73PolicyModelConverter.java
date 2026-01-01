package com.github.nagyesta.lowkeyvault.mapper.v7_3.certificate;

import com.github.nagyesta.lowkeyvault.mapper.common.AliasAwareConverter;
import com.github.nagyesta.lowkeyvault.model.v7_3.certificate.CertificatePolicyModel;
import com.github.nagyesta.lowkeyvault.service.certificate.ReadOnlyKeyVaultCertificateEntity;
import org.jspecify.annotations.Nullable;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;

import java.net.URI;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {
                CertificateEntityToV73PropertiesModelConverter.class,
                BaseCertificateEntityToV73PolicyModelConverter.class
        }
)
public interface CertificateEntityToV73PolicyModelConverter
        extends AliasAwareConverter<ReadOnlyKeyVaultCertificateEntity, CertificatePolicyModel> {


    @Override
    default @Nullable CertificatePolicyModel convert(
            @Nullable final ReadOnlyKeyVaultCertificateEntity source,
            final URI vaultUri) {
        if (source == null) {
            return null;
        }
        return doConvert(source, vaultUri);
    }

    @Named("ignore")
    @Mapping(target = "id", expression = "java(source.getId().asPolicyUri(vaultUri).toString())")
    @Mapping(target = "attributes", source = "source")
    @Mapping(target = "issuer", source = "source.originalCertificatePolicy")
    @Mapping(target = "keyProperties", source = "source.originalCertificatePolicy")
    @Mapping(target = "secretProperties", source = "source.originalCertificatePolicy")
    @Mapping(target = "x509Properties", source = "source.originalCertificatePolicy")
    @Mapping(target = "lifetimeActions", ignore = true)
    CertificatePolicyModel doConvert(ReadOnlyKeyVaultCertificateEntity source, URI vaultUri);

}
