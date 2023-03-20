package com.github.nagyesta.lowkeyvault.model.common.backup;

import com.github.nagyesta.lowkeyvault.model.v7_2.common.BaseBackupListItem;
import com.github.nagyesta.lowkeyvault.model.v7_3.certificate.CertificatePropertiesModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CertificateBackupListItem extends BaseBackupListItem<CertificatePropertiesModel> {

    //add all certificate properties here, which are needed for successful import
}
