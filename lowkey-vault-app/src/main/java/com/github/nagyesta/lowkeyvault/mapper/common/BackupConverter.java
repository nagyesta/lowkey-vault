package com.github.nagyesta.lowkeyvault.mapper.common;

import com.github.nagyesta.lowkeyvault.model.v7_2.BasePropertiesModel;
import com.github.nagyesta.lowkeyvault.model.v7_2.common.BaseBackupListItem;
import com.github.nagyesta.lowkeyvault.service.EntityId;
import com.github.nagyesta.lowkeyvault.service.common.BaseVaultEntity;
import org.springframework.lang.NonNull;

import java.util.Map;

public abstract class BackupConverter<K extends EntityId, V extends K, E extends BaseVaultEntity<V>, P extends BasePropertiesModel,
        BLI extends BaseBackupListItem<P>> implements ApiVersionAwareConverter<E, BLI> {

    @NonNull
    @Override
    public BLI convert(@NonNull final E source) {
        final var item = convertUniqueFields(source);
        return mapCommonFields(source, item);
    }

    protected abstract BLI convertUniqueFields(E source);

    private BLI mapCommonFields(final E source, final BLI item) {
        final var entityId = source.getId();
        item.setVaultBaseUri(entityId.vault());
        item.setId(entityId.id());
        item.setVersion(entityId.version());
        item.setAttributes(propertiesConverter().convert(source, entityId.vault()));
        item.setTags(Map.copyOf(source.getTags()));
        item.setManaged(source.isManaged());
        return item;
    }

    protected abstract AliasAwareConverter<E, P> propertiesConverter();
}
