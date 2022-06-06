package com.github.nagyesta.lowkeyvault.service.key;

import com.github.nagyesta.lowkeyvault.model.v7_3.key.constants.LifetimeActionType;

public interface LifetimeAction {

    LifetimeActionType getActionType();

    LifetimeActionTrigger getTrigger();
}
