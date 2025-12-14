package com.github.nagyesta.lowkeyvault.mapper.v7_3.certificate;

import com.github.nagyesta.lowkeyvault.model.v7_3.certificate.CertificatePropertiesModel;
import com.github.nagyesta.lowkeyvault.service.certificate.ReadOnlyKeyVaultCertificateEntity;
import org.jspecify.annotations.Nullable;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;

import java.time.OffsetDateTime;
import java.util.Optional;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CertificateEntityToV73PropertiesModelConverter {

    @Mapping(target = "expiry", qualifiedByName = "optionalOffsetDateTimeConverter")
    @Mapping(target = "notBefore", qualifiedByName = "optionalOffsetDateTimeConverter")
    @Nullable CertificatePropertiesModel convert(@Nullable ReadOnlyKeyVaultCertificateEntity source);

    @Named("optionalOffsetDateTimeConverter")
    default @Nullable OffsetDateTime optionalOffsetDateTimeConverter(final Optional<OffsetDateTime> input) {
        return input.orElse(null);
    }
}
