package com.github.nagyesta.lowkeyvault.mapper.v7_2.secret;

import com.github.nagyesta.lowkeyvault.context.ApiVersionAware;
import com.github.nagyesta.lowkeyvault.mapper.common.AliasAwareConverter;
import com.github.nagyesta.lowkeyvault.mapper.common.BasePropertiesModelConverter;
import com.github.nagyesta.lowkeyvault.mapper.common.registry.SecretConverterRegistry;
import com.github.nagyesta.lowkeyvault.model.v7_2.secret.SecretPropertiesModel;
import com.github.nagyesta.lowkeyvault.service.secret.ReadOnlyKeyVaultSecretEntity;
import com.github.nagyesta.lowkeyvault.service.secret.id.VersionedSecretEntityId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;

import java.net.URI;
import java.util.SortedSet;

public class SecretEntityToV72PropertiesModelConverter
        extends BasePropertiesModelConverter<VersionedSecretEntityId, ReadOnlyKeyVaultSecretEntity, SecretPropertiesModel>
        implements AliasAwareConverter<ReadOnlyKeyVaultSecretEntity, SecretPropertiesModel> {

    private final SecretConverterRegistry registry;

    @Autowired
    public SecretEntityToV72PropertiesModelConverter(@lombok.NonNull final SecretConverterRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void afterPropertiesSet() {
        registry.registerPropertiesConverter(this);
    }

    @Override
    @NonNull
    public SecretPropertiesModel convert(@NonNull final ReadOnlyKeyVaultSecretEntity source, @NonNull final URI vaultUri) {
        return mapCommonFields(source, new SecretPropertiesModel());
    }

    @Override
    public SortedSet<String> supportedVersions() {
        return ApiVersionAware.V7_2_AND_V7_3;
    }
}
