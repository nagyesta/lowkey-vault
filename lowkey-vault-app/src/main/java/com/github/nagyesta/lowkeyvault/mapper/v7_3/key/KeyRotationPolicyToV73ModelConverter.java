package com.github.nagyesta.lowkeyvault.mapper.v7_3.key;

import com.github.nagyesta.lowkeyvault.mapper.common.AliasAwareConverter;
import com.github.nagyesta.lowkeyvault.model.v7_3.key.*;
import com.github.nagyesta.lowkeyvault.model.v7_3.key.constants.LifetimeActionType;
import com.github.nagyesta.lowkeyvault.service.key.KeyLifetimeAction;
import com.github.nagyesta.lowkeyvault.service.key.LifetimeAction;
import com.github.nagyesta.lowkeyvault.service.key.LifetimeActionTrigger;
import com.github.nagyesta.lowkeyvault.service.key.ReadOnlyRotationPolicy;
import org.jspecify.annotations.Nullable;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.net.URI;
import java.util.List;
import java.util.Map;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface KeyRotationPolicyToV73ModelConverter
        extends AliasAwareConverter<ReadOnlyRotationPolicy, KeyRotationPolicyModel> {


    @Mapping(target = "id", expression = "java(source.getId().asRotationPolicyUri(vaultUri))")
    @Mapping(target = "attributes", expression = "java(convertAttributes(source))")
    @Mapping(target = "lifetimeActions", expression = "java(convertLifetimeActions(source.getLifetimeActions()))")
    @Mapping(target = "keyEntityId", ignore = true)
    @Override
    @Nullable KeyRotationPolicyModel convert(@Nullable ReadOnlyRotationPolicy source, URI vaultUri);

    KeyRotationPolicyAttributes convertAttributes(ReadOnlyRotationPolicy source);

    default List<KeyLifetimeActionModel> convertLifetimeActions(final Map<LifetimeActionType, LifetimeAction> lifetimeActions) {
        return lifetimeActions.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> this.convertLifetimeAction((KeyLifetimeAction) e.getValue()))
                .toList();
    }

    @Mapping(target = "trigger", source = "source.trigger")
    @Mapping(target = "action", source = "source.actionType")
    KeyLifetimeActionModel convertLifetimeAction(KeyLifetimeAction source);

    default KeyLifetimeActionTypeModel convertLifetimeActionTYpe(final LifetimeActionType actionType) {
        return new KeyLifetimeActionTypeModel(actionType);
    }

    default KeyLifetimeActionTriggerModel convertLifetimeActionTrigger(final LifetimeActionTrigger trigger) {
        return new KeyLifetimeActionTriggerModel(trigger);
    }

}
