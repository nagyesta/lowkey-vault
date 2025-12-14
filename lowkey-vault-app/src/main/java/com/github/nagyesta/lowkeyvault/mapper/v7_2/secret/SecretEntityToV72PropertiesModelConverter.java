package com.github.nagyesta.lowkeyvault.mapper.v7_2.secret;

import com.github.nagyesta.lowkeyvault.model.v7_2.secret.SecretPropertiesModel;
import com.github.nagyesta.lowkeyvault.service.secret.ReadOnlyKeyVaultSecretEntity;
import org.jspecify.annotations.Nullable;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;

import java.time.OffsetDateTime;
import java.util.Optional;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface SecretEntityToV72PropertiesModelConverter {

    @Mapping(target = "expiry", qualifiedByName = "optionalOffsetDateTimeConverter")
    @Mapping(target = "notBefore", qualifiedByName = "optionalOffsetDateTimeConverter")
    @Nullable SecretPropertiesModel convert(@Nullable ReadOnlyKeyVaultSecretEntity source);

    @Named("optionalOffsetDateTimeConverter")
    default @Nullable OffsetDateTime optionalOffsetDateTimeConverter(final Optional<OffsetDateTime> input) {
        return input.orElse(null);
    }
}
