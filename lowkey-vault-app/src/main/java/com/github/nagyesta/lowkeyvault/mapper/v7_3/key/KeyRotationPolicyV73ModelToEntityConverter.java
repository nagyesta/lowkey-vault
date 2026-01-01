package com.github.nagyesta.lowkeyvault.mapper.v7_3.key;

import com.github.nagyesta.lowkeyvault.mapper.common.BaseConverter;
import com.github.nagyesta.lowkeyvault.model.v7_3.key.KeyLifetimeActionModel;
import com.github.nagyesta.lowkeyvault.model.v7_3.key.KeyRotationPolicyModel;
import com.github.nagyesta.lowkeyvault.model.v7_3.key.constants.LifetimeActionType;
import com.github.nagyesta.lowkeyvault.service.key.KeyLifetimeAction;
import com.github.nagyesta.lowkeyvault.service.key.LifetimeAction;
import com.github.nagyesta.lowkeyvault.service.key.RotationPolicy;
import com.github.nagyesta.lowkeyvault.service.key.impl.KeyLifetimeActionTrigger;
import com.github.nagyesta.lowkeyvault.service.key.impl.KeyRotationPolicy;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class KeyRotationPolicyV73ModelToEntityConverter implements BaseConverter<KeyRotationPolicyModel, RotationPolicy> {

    @Override
    public @Nullable RotationPolicy convert(@Nullable final KeyRotationPolicyModel source) {
        return Optional.ofNullable(source)
                .filter(this::isNotEmpty)
                .map(this::convertNonNull)
                .orElse(null);
    }

    public RotationPolicy convertNonNull(final KeyRotationPolicyModel source) {
        final var keyEntityId = source.getKeyEntityId();
        Assert.notNull(keyEntityId, "EntityId cannot be null.");
        Assert.notNull(source.getAttributes(), "Attributes cannot be null.");
        Assert.notNull(source.getLifetimeActions(), "LifetimeActions cannot be null.");
        Assert.notEmpty(source.getLifetimeActions(), "LifetimeActions cannot be empty.");
        final var actions = convertLifetimeActions(source.getLifetimeActions());
        final var expiryTime = source.getAttributes().getExpiryTime();
        final RotationPolicy entity = new KeyRotationPolicy(keyEntityId, expiryTime, actions);
        entity.setCreated(Objects.requireNonNullElse(source.getAttributes().getCreated(), OffsetDateTime.now()));
        entity.setUpdated(Objects.requireNonNullElse(source.getAttributes().getUpdated(), OffsetDateTime.now()));
        return entity;
    }

    private boolean isNotEmpty(final KeyRotationPolicyModel rotationPolicyModel) {
        return !CollectionUtils.isEmpty(rotationPolicyModel.getLifetimeActions());
    }

    private Map<LifetimeActionType, LifetimeAction> convertLifetimeActions(final List<KeyLifetimeActionModel> lifetimeActions) {
        return lifetimeActions.stream()
                .map(this::convertLifetimeAction)
                .collect(Collectors.toMap(LifetimeAction::actionType, Function.identity()));
    }

    private LifetimeAction convertLifetimeAction(final KeyLifetimeActionModel source) {
        final var sourceTrigger = Objects.requireNonNull(source.getTrigger());
        final var action = Objects.requireNonNull(source.getAction());
        final var trigger = new KeyLifetimeActionTrigger(
                Objects.requireNonNull(sourceTrigger.getTriggerPeriod()),
                Objects.requireNonNull(sourceTrigger.getTriggerType()));
        return new KeyLifetimeAction(action.getType(), trigger);
    }
}
