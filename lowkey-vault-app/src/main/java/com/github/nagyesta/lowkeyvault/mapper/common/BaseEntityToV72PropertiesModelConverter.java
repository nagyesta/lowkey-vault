package com.github.nagyesta.lowkeyvault.mapper.common;

import com.github.nagyesta.lowkeyvault.model.v7_2.BasePropertiesModel;
import com.github.nagyesta.lowkeyvault.service.common.BaseVaultEntity;

public class BaseEntityToV72PropertiesModelConverter {

    protected <M extends BasePropertiesModel> M mapCommonFields(final BaseVaultEntity<?> entity, final M attributes) {
        attributes.setCreatedOn(entity.getCreated());
        attributes.setUpdatedOn(entity.getUpdated());
        attributes.setEnabled(entity.isEnabled());
        entity.getExpiry().ifPresent(attributes::setExpiresOn);
        entity.getNotBefore().ifPresent(attributes::setNotBefore);
        attributes.setRecoveryLevel(entity.getRecoveryLevel());
        attributes.setRecoverableDays(entity.getRecoverableDays());
        return attributes;
    }
}
