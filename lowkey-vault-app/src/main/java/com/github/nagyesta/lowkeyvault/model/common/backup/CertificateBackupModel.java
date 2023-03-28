package com.github.nagyesta.lowkeyvault.model.common.backup;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.github.nagyesta.lowkeyvault.model.json.util.Base64ZipCertificateDeserializer;
import com.github.nagyesta.lowkeyvault.model.json.util.Base64ZipCertificateSerializer;
import com.github.nagyesta.lowkeyvault.model.v7_2.common.BaseBackupModel;
import com.github.nagyesta.lowkeyvault.model.v7_3.certificate.CertificatePropertiesModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CertificateBackupModel extends BaseBackupModel<CertificatePropertiesModel, CertificateBackupListItem, CertificateBackupList> {

    @JsonSerialize(using = Base64ZipCertificateSerializer.class)
    @Override
    public CertificateBackupList getValue() {
        return super.getValue();
    }

    @JsonDeserialize(using = Base64ZipCertificateDeserializer.class)
    @Override
    public void setValue(final CertificateBackupList value) {
        super.setValue(value);
    }
}
