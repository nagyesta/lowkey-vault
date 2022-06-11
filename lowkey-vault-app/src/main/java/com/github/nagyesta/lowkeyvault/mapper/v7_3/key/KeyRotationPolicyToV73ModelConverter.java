package com.github.nagyesta.lowkeyvault.mapper.v7_3.key;

import com.github.nagyesta.lowkeyvault.model.v7_3.key.*;
import com.github.nagyesta.lowkeyvault.model.v7_3.key.constants.LifetimeActionType;
import com.github.nagyesta.lowkeyvault.service.key.LifetimeAction;
import com.github.nagyesta.lowkeyvault.service.key.ReadOnlyRotationPolicy;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class KeyRotationPolicyToV73ModelConverter implements Converter<ReadOnlyRotationPolicy, KeyRotationPolicyModel> {

    @Override
    public KeyRotationPolicyModel convert(@Nullable final ReadOnlyRotationPolicy source) {
        return Optional.ofNullable(source)
                .map(this::convertNonNull)
                .orElse(null);
    }

    private KeyRotationPolicyModel convertNonNull(final ReadOnlyRotationPolicy readOnlyRotationPolicy) {
        final KeyRotationPolicyModel model = new KeyRotationPolicyModel();
        model.setId(readOnlyRotationPolicy.getId().asRotationPolicyUri());
        model.setAttributes(convertAttributes(readOnlyRotationPolicy));
        model.setLifetimeActions(convertLifetimeActions(readOnlyRotationPolicy.getLifetimeActions()));
        return model;
    }

    private KeyRotationPolicyAttributes convertAttributes(final ReadOnlyRotationPolicy readOnlyRotationPolicy) {
        final KeyRotationPolicyAttributes attributes = new KeyRotationPolicyAttributes();
        attributes.setExpiryTime(readOnlyRotationPolicy.getExpiryTime());
        attributes.setCreated(readOnlyRotationPolicy.getCreatedOn());
        attributes.setUpdated(readOnlyRotationPolicy.getUpdatedOn());
        return attributes;
    }

    private List<KeyLifetimeActionModel> convertLifetimeActions(final Map<LifetimeActionType, LifetimeAction> lifetimeActions) {
        return lifetimeActions.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> this.convertLifetimeAction(e.getValue()))
                .collect(Collectors.toList());
    }

    private KeyLifetimeActionModel convertLifetimeAction(final LifetimeAction lifetimeAction) {
        return new KeyLifetimeActionModel(
                new KeyLifetimeActionTypeModel(lifetimeAction.getActionType()),
                new KeyLifetimeActionTriggerModel(lifetimeAction.getTrigger()));
    }
}
