package com.github.nagyesta.lowkeyvault.mapper.common;

import com.github.nagyesta.lowkeyvault.model.management.VaultModel;
import com.github.nagyesta.lowkeyvault.service.vault.VaultFake;
import org.jspecify.annotations.Nullable;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;

import java.net.URI;
import java.util.Set;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface VaultFakeToVaultModelConverter {

    @Mapping(target = "baseUri", source = "source", qualifiedByName = "vaultBaseUriConverter")
    @Mapping(target = "aliases", source = "source", qualifiedByName = "vaultAliasConverter")
    @Nullable VaultModel convert(@Nullable VaultFake source);

    @Named("vaultBaseUriConverter")
    default URI baseUri(final VaultFake source) {
        return source.baseUri();
    }

    @Named("vaultAliasConverter")
    default Set<URI> aliases(final VaultFake source) {
        return source.aliases();
    }
}
