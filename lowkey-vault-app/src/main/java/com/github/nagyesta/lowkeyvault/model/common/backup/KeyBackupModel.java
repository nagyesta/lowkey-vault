package com.github.nagyesta.lowkeyvault.model.common.backup;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.github.nagyesta.lowkeyvault.model.json.util.Base64ZipKeyDeserializer;
import com.github.nagyesta.lowkeyvault.model.json.util.Base64ZipKeySerializer;
import com.github.nagyesta.lowkeyvault.model.v7_2.common.BaseBackupModel;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.KeyPropertiesModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class KeyBackupModel
        extends BaseBackupModel<KeyPropertiesModel, KeyBackupListItem, KeyBackupList> {

    @JsonSerialize(using = Base64ZipKeySerializer.class)
    @Override
    public KeyBackupList getValue() {
        return super.getValue();
    }

    @JsonDeserialize(using = Base64ZipKeyDeserializer.class)
    @Override
    public void setValue(final KeyBackupList value) {
        super.setValue(value);
    }
}
