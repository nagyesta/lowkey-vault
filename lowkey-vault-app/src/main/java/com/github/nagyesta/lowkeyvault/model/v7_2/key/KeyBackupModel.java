package com.github.nagyesta.lowkeyvault.model.v7_2.key;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.github.nagyesta.lowkeyvault.model.json.util.Base64ZipV72KeyDeserializer;
import com.github.nagyesta.lowkeyvault.model.json.util.Base64ZipV72KeySerializer;
import com.github.nagyesta.lowkeyvault.model.v7_2.common.BaseBackupModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class KeyBackupModel extends BaseBackupModel<KeyPropertiesModel, KeyBackupListItem, KeyBackupList> {

    @JsonSerialize(using = Base64ZipV72KeySerializer.class)
    @Override
    public KeyBackupList getValue() {
        return super.getValue();
    }

    @JsonDeserialize(using = Base64ZipV72KeyDeserializer.class)
    @Override
    public void setValue(final KeyBackupList value) {
        super.setValue(value);
    }
}
