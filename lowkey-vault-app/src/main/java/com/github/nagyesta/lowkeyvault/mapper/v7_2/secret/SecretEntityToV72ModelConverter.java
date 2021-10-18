package com.github.nagyesta.lowkeyvault.mapper.v7_2.secret;

import com.github.nagyesta.lowkeyvault.mapper.common.BaseRecoveryAwareConverter;
import com.github.nagyesta.lowkeyvault.model.v7_2.secret.DeletedKeyVaultSecretModel;
import com.github.nagyesta.lowkeyvault.model.v7_2.secret.KeyVaultSecretModel;
import com.github.nagyesta.lowkeyvault.service.secret.ReadOnlyKeyVaultSecretEntity;
import com.github.nagyesta.lowkeyvault.service.secret.id.VersionedSecretEntityId;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SecretEntityToV72ModelConverter
        extends BaseRecoveryAwareConverter<VersionedSecretEntityId, ReadOnlyKeyVaultSecretEntity, KeyVaultSecretModel,
        DeletedKeyVaultSecretModel> {

    private final SecretEntityToV72PropertiesModelConverter secretEntityToV72PropertiesModelConverter;

    @Autowired
    public SecretEntityToV72ModelConverter(
            @NonNull final SecretEntityToV72PropertiesModelConverter secretEntityToV72PropertiesModelConverter) {
        super(KeyVaultSecretModel::new, DeletedKeyVaultSecretModel::new);
        this.secretEntityToV72PropertiesModelConverter = secretEntityToV72PropertiesModelConverter;
    }

    @Override
    protected <M extends KeyVaultSecretModel> M mapActiveFields(final ReadOnlyKeyVaultSecretEntity source, final M model) {
        model.setId(source.getUri().toString());
        model.setContentType(source.getContentType());
        model.setValue(source.getValue());
        model.setAttributes(secretEntityToV72PropertiesModelConverter.convert(source));
        model.setTags(source.getTags());
        return model;
    }
}
