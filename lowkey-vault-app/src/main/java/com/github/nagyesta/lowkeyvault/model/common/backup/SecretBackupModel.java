package com.github.nagyesta.lowkeyvault.model.common.backup;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.github.nagyesta.lowkeyvault.model.json.util.Base64ZipSecretDeserializer;
import com.github.nagyesta.lowkeyvault.model.json.util.Base64ZipSecretSerializer;
import com.github.nagyesta.lowkeyvault.model.v7_2.common.BaseBackupModel;
import com.github.nagyesta.lowkeyvault.model.v7_2.secret.SecretPropertiesModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SecretBackupModel extends BaseBackupModel<SecretPropertiesModel, SecretBackupListItem, SecretBackupList> {

    @JsonSerialize(using = Base64ZipSecretSerializer.class)
    @Override
    public SecretBackupList getValue() {
        return super.getValue();
    }

    @JsonDeserialize(using = Base64ZipSecretDeserializer.class)
    @Override
    public void setValue(final SecretBackupList value) {
        super.setValue(value);
    }
}
