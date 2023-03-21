package com.github.nagyesta.lowkeyvault.mapper.v7_2.key;

import com.github.nagyesta.lowkeyvault.context.ApiVersionAware;
import com.github.nagyesta.lowkeyvault.mapper.common.BaseRecoveryAwareConverter;
import com.github.nagyesta.lowkeyvault.mapper.common.registry.KeyConverterRegistry;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.DeletedKeyVaultKeyItemModel;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.KeyVaultKeyItemModel;
import com.github.nagyesta.lowkeyvault.service.key.ReadOnlyKeyVaultKeyEntity;
import com.github.nagyesta.lowkeyvault.service.key.id.VersionedKeyEntityId;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.URI;
import java.util.SortedSet;

public class KeyEntityToV72KeyItemModelConverter
        extends BaseRecoveryAwareConverter<VersionedKeyEntityId,
        ReadOnlyKeyVaultKeyEntity, KeyVaultKeyItemModel, DeletedKeyVaultKeyItemModel> {

    private final KeyConverterRegistry registry;

    @Autowired
    public KeyEntityToV72KeyItemModelConverter(
            @NonNull final KeyConverterRegistry registry) {
        super(KeyVaultKeyItemModel::new, DeletedKeyVaultKeyItemModel::new);
        this.registry = registry;
    }

    @Override
    public void afterPropertiesSet() {
        register(registry);
    }

    protected void register(final KeyConverterRegistry registry) {
        registry.registerItemConverter(this);
    }

    @Override
    protected <M extends KeyVaultKeyItemModel> M mapActiveFields(
            final ReadOnlyKeyVaultKeyEntity source, final M model, final URI vaultUri) {
        model.setKeyId(convertKeyId(source, vaultUri));
        model.setAttributes(registry.propertiesConverter(supportedVersions().last()).convert(source, vaultUri));
        model.setTags(source.getTags());
        return model;
    }

    protected String convertKeyId(final ReadOnlyKeyVaultKeyEntity source, final URI vaultUri) {
        return source.getId().asUriNoVersion(vaultUri).toString();
    }

    @Override
    public SortedSet<String> supportedVersions() {
        return ApiVersionAware.V7_2_AND_V7_3;
    }
}
