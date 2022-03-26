package com.github.nagyesta.lowkeyvault.mapper.common;

import com.github.nagyesta.lowkeyvault.model.v7_2.BasePropertiesModel;
import com.github.nagyesta.lowkeyvault.model.v7_2.common.BaseBackupListItem;
import com.github.nagyesta.lowkeyvault.service.EntityId;
import com.github.nagyesta.lowkeyvault.service.common.BaseVaultEntity;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;

import java.util.Map;

public abstract class BackupConverter<V extends EntityId, E extends BaseVaultEntity<? extends V>, P extends BasePropertiesModel,
        BLI extends BaseBackupListItem<P>> implements Converter<E, BLI> {

    private final Converter<E, P> propertiesConverter;

    protected BackupConverter(@lombok.NonNull final Converter<E, P> propertiesConverter) {
        this.propertiesConverter = propertiesConverter;
    }

    @NonNull
    @Override
    public BLI convert(@NonNull final E source) {
        final BLI item = convertUniqueFields(source);
        return mapCommonFields(source, item);
    }

    protected abstract BLI convertUniqueFields(E source);

    private BLI mapCommonFields(final E source, final BLI item) {
        final V entityId = source.getId();
        item.setVaultBaseUri(entityId.vault());
        item.setId(entityId.id());
        item.setVersion(entityId.version());
        item.setAttributes(propertiesConverter.convert(source));
        item.setTags(Map.copyOf(source.getTags()));
        item.setManaged(source.isManaged());
        return item;
    }
}
