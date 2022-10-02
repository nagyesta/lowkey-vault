package com.github.nagyesta.lowkeyvault.mapper.v7_2.secret;

import com.github.nagyesta.lowkeyvault.mapper.AliasAwareConverter;
import com.github.nagyesta.lowkeyvault.mapper.common.BaseEntityToV72PropertiesModelConverter;
import com.github.nagyesta.lowkeyvault.model.v7_2.secret.SecretPropertiesModel;
import com.github.nagyesta.lowkeyvault.service.secret.ReadOnlyKeyVaultSecretEntity;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.net.URI;

@Component
public class SecretEntityToV72PropertiesModelConverter
        extends BaseEntityToV72PropertiesModelConverter
        implements AliasAwareConverter<ReadOnlyKeyVaultSecretEntity, SecretPropertiesModel> {

    @Override
    @NonNull
    public SecretPropertiesModel convert(@NonNull final ReadOnlyKeyVaultSecretEntity source, @NonNull final URI vaultUri) {
        return mapCommonFields(source, new SecretPropertiesModel());
    }

}
