package com.github.nagyesta.lowkeyvault.mapper.common;

import com.github.nagyesta.lowkeyvault.model.management.VaultModel;
import com.github.nagyesta.lowkeyvault.service.vault.VaultFake;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class VaultFakeToVaultModelConverter
        implements Converter<VaultFake, VaultModel> {

    @Override
    public VaultModel convert(@Nullable final VaultFake source) {
        return Optional.ofNullable(source)
                .map(this::convertNonNull)
                .orElse(null);
    }

    @NonNull
    public VaultModel convertNonNull(@NonNull final VaultFake fake) {
        final var model = new VaultModel();
        model.setBaseUri(fake.baseUri());
        model.setAliases(fake.aliases());
        model.setRecoveryLevel(fake.getRecoveryLevel());
        model.setRecoverableDays(fake.getRecoverableDays());
        model.setCreatedOn(fake.getCreatedOn());
        model.setDeletedOn(fake.getDeletedOn());
        return model;
    }
}
