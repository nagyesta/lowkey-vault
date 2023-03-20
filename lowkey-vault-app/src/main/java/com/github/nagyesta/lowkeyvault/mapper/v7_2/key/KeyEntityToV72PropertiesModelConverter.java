package com.github.nagyesta.lowkeyvault.mapper.v7_2.key;

import com.github.nagyesta.lowkeyvault.context.ApiVersionAware;
import com.github.nagyesta.lowkeyvault.mapper.common.AliasAwareConverter;
import com.github.nagyesta.lowkeyvault.mapper.common.BasePropertiesModelConverter;
import com.github.nagyesta.lowkeyvault.mapper.common.registry.KeyConverterRegistry;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.KeyPropertiesModel;
import com.github.nagyesta.lowkeyvault.service.key.ReadOnlyKeyVaultKeyEntity;
import com.github.nagyesta.lowkeyvault.service.key.id.VersionedKeyEntityId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;

import java.net.URI;
import java.util.SortedSet;

public class KeyEntityToV72PropertiesModelConverter
        extends BasePropertiesModelConverter<VersionedKeyEntityId, ReadOnlyKeyVaultKeyEntity, KeyPropertiesModel>
        implements AliasAwareConverter<ReadOnlyKeyVaultKeyEntity, KeyPropertiesModel> {

    private final KeyConverterRegistry registry;

    @Autowired
    public KeyEntityToV72PropertiesModelConverter(@lombok.NonNull final KeyConverterRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void afterPropertiesSet() {
        registry.registerPropertiesConverter(this);
    }

    @Override
    @NonNull
    public KeyPropertiesModel convert(
            @org.springframework.lang.NonNull final ReadOnlyKeyVaultKeyEntity source,
            @org.springframework.lang.NonNull final URI vaultUri) {
        return mapCommonFields(source, new KeyPropertiesModel());
    }

    @Override
    public SortedSet<String> supportedVersions() {
        return ApiVersionAware.V7_2_AND_V7_3;
    }
}
