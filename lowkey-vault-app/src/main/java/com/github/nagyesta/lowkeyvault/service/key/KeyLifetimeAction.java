package com.github.nagyesta.lowkeyvault.service.key;

import com.github.nagyesta.lowkeyvault.model.v7_3.key.constants.LifetimeActionType;
import lombok.NonNull;

public record KeyLifetimeAction(
        @NonNull LifetimeActionType actionType,
        @NonNull LifetimeActionTrigger trigger) implements LifetimeAction {

}
