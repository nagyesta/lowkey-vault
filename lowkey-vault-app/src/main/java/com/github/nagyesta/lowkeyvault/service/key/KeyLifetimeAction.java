package com.github.nagyesta.lowkeyvault.service.key;

import com.github.nagyesta.lowkeyvault.model.v7_3.key.constants.LifetimeActionType;
import lombok.NonNull;

public class KeyLifetimeAction implements LifetimeAction {

    private final LifetimeActionType actionType;
    private final LifetimeActionTrigger trigger;

    public KeyLifetimeAction(@NonNull final LifetimeActionType actionType, @NonNull final LifetimeActionTrigger trigger) {
        this.actionType = actionType;
        this.trigger = trigger;
    }

    @Override
    public LifetimeActionType getActionType() {
        return actionType;
    }

    @Override
    public LifetimeActionTrigger getTrigger() {
        return trigger;
    }
}
