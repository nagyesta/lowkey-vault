package com.github.nagyesta.lowkeyvault.mapper.v7_3.key;

import com.github.nagyesta.lowkeyvault.context.ApiVersionAware;
import com.github.nagyesta.lowkeyvault.mapper.common.ApiVersionAwareConverter;
import com.github.nagyesta.lowkeyvault.mapper.common.registry.KeyConverterRegistry;
import com.github.nagyesta.lowkeyvault.model.v7_3.key.*;
import com.github.nagyesta.lowkeyvault.model.v7_3.key.constants.LifetimeActionType;
import com.github.nagyesta.lowkeyvault.service.key.KeyLifetimeAction;
import com.github.nagyesta.lowkeyvault.service.key.LifetimeAction;
import com.github.nagyesta.lowkeyvault.service.key.RotationPolicy;
import com.github.nagyesta.lowkeyvault.service.key.impl.KeyLifetimeActionTrigger;
import com.github.nagyesta.lowkeyvault.service.key.impl.KeyRotationPolicy;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.time.OffsetDateTime;
import java.time.Period;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class KeyRotationPolicyV73ModelToEntityConverter implements ApiVersionAwareConverter<KeyRotationPolicyModel, RotationPolicy> {

    private final KeyConverterRegistry registry;

    @Autowired
    public KeyRotationPolicyV73ModelToEntityConverter(@NonNull final KeyConverterRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        registry.registerRotationPolicyEntityConverter(this);
    }

    @Override
    public RotationPolicy convert(@Nullable final KeyRotationPolicyModel source) {
        return Optional.ofNullable(source)
                .filter(this::isNotEmpty)
                .map(this::convertNonNull)
                .orElse(null);
    }

    private boolean isNotEmpty(final KeyRotationPolicyModel rotationPolicyModel) {
        return hasLifetimeActions(rotationPolicyModel) || hasAttributes(rotationPolicyModel);
    }

    private boolean hasAttributes(final KeyRotationPolicyModel rotationPolicyModel) {
        return rotationPolicyModel.getAttributes() != null;
    }

    private RotationPolicy convertNonNull(final KeyRotationPolicyModel source) {
        Assert.notNull(source.getKeyEntityId(), "EntityId cannot be null.");
        Assert.notNull(source.getAttributes(), "Attributes cannot be null.");
        Assert.notNull(source.getLifetimeActions(), "LifetimeActions cannot be null.");
        Assert.notEmpty(source.getLifetimeActions(), "LifetimeActions cannot be empty.");
        final Map<LifetimeActionType, LifetimeAction> actions = convertLifetimeActions(source.getLifetimeActions());
        final Period expiryTime = source.getAttributes().getExpiryTime();
        final RotationPolicy entity = new KeyRotationPolicy(source.getKeyEntityId(), expiryTime, actions);
        return convertAttributes(source.getAttributes(), entity);
    }

    private boolean hasLifetimeActions(final KeyRotationPolicyModel source) {
        return source.getLifetimeActions() != null && !source.getLifetimeActions().isEmpty();
    }

    private RotationPolicy convertAttributes(final KeyRotationPolicyAttributes source, final RotationPolicy entity) {
        entity.setCreatedOn(Optional.ofNullable(source).map(KeyRotationPolicyAttributes::getCreated).orElse(OffsetDateTime.now()));
        entity.setUpdatedOn(Optional.ofNullable(source).map(KeyRotationPolicyAttributes::getUpdated).orElse(OffsetDateTime.now()));
        return entity;
    }

    private Map<LifetimeActionType, LifetimeAction> convertLifetimeActions(final List<KeyLifetimeActionModel> lifetimeActions) {
        return lifetimeActions.stream()
                .map(this::convertLifetimeAction)
                .collect(Collectors.toMap(LifetimeAction::actionType, Function.identity()));
    }

    private LifetimeAction convertLifetimeAction(final KeyLifetimeActionModel source) {
        final KeyLifetimeActionTriggerModel sourceTrigger = Objects.requireNonNull(source.getTrigger());
        final KeyLifetimeActionTypeModel action = Objects.requireNonNull(source.getAction());
        final KeyLifetimeActionTrigger trigger = new KeyLifetimeActionTrigger(
                sourceTrigger.getTriggerPeriod(), sourceTrigger.getTriggerType());
        return new KeyLifetimeAction(action.getType(), trigger);
    }

    @Override
    public SortedSet<String> supportedVersions() {
        return ApiVersionAware.V7_3_AND_LATER;
    }
}
