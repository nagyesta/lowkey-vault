package com.github.nagyesta.lowkeyvault.service.key;

import com.github.nagyesta.lowkeyvault.model.v7_3.key.constants.LifetimeActionType;
import lombok.NonNull;

public record KeyLifetimeAction(LifetimeActionType actionType, LifetimeActionTrigger trigger) implements LifetimeAction {

    public KeyLifetimeAction(@NonNull final LifetimeActionType actionType, @NonNull final LifetimeActionTrigger trigger) {
        this.actionType = actionType;
        this.trigger = trigger;
    }
}
