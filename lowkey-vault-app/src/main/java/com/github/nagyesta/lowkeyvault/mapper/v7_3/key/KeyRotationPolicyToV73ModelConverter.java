package com.github.nagyesta.lowkeyvault.mapper.v7_3.key;

import com.github.nagyesta.lowkeyvault.mapper.common.AliasAwareConverter;
import com.github.nagyesta.lowkeyvault.model.v7_3.key.*;
import com.github.nagyesta.lowkeyvault.model.v7_3.key.constants.LifetimeActionType;
import com.github.nagyesta.lowkeyvault.service.key.KeyLifetimeAction;
import com.github.nagyesta.lowkeyvault.service.key.LifetimeAction;
import com.github.nagyesta.lowkeyvault.service.key.LifetimeActionTrigger;
import com.github.nagyesta.lowkeyvault.service.key.ReadOnlyRotationPolicy;
import org.jspecify.annotations.Nullable;
import org.mapstruct.*;

import java.net.URI;
import java.util.List;
import java.util.Map;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface KeyRotationPolicyToV73ModelConverter
        extends AliasAwareConverter<ReadOnlyRotationPolicy, KeyRotationPolicyModel> {


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "attributes", source = "source")
    @Mapping(target = "lifetimeActions", source = "source.lifetimeActions")
    @Mapping(target = "keyEntityId", ignore = true)
    @Override
    @Nullable KeyRotationPolicyModel convert(@Nullable ReadOnlyRotationPolicy source, URI vaultUri);

    @AfterMapping
    default void postProcess(
            @Nullable final ReadOnlyRotationPolicy source,
            final URI vaultUri,
            @Nullable @MappingTarget final KeyRotationPolicyModel model) {
        if (source != null && model != null) {
            model.setId(source.getId().asRotationPolicyUri(vaultUri));
        }
    }

    @Nullable KeyRotationPolicyAttributes convertAttributes(@Nullable ReadOnlyRotationPolicy source);

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
