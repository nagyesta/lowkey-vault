package com.github.nagyesta.lowkeyvault.model.v7_3.key;


import com.fasterxml.jackson.annotation.*;
import com.github.nagyesta.lowkeyvault.service.key.LifetimeActionTrigger;
import com.github.nagyesta.lowkeyvault.service.key.constants.LifetimeActionTriggerType;
import lombok.Data;
import org.jspecify.annotations.Nullable;
import org.springframework.util.Assert;

import java.time.Period;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class KeyLifetimeActionTriggerModel {

    @Nullable
    @JsonIgnore
    private LifetimeActionTriggerType triggerType;
    @Nullable
    @JsonIgnore
    private Period triggerPeriod;

    @JsonCreator
    public KeyLifetimeActionTriggerModel(
            @Nullable @JsonProperty("timeBeforeExpiry") final Period timeBeforeExpiry,
            @Nullable @JsonProperty("timeAfterCreate") final Period timeAfterCreate) {
        Assert.isTrue(timeBeforeExpiry == null || timeAfterCreate == null,
                "TimeBeforeExpiry and TimeAfterCreate cannot be populated at the same time.");
        Assert.isTrue(timeBeforeExpiry != null || timeAfterCreate != null,
                "TimeBeforeExpiry and TimeAfterCreate cannot be null at the same time.");
        if (timeAfterCreate != null) {
            this.triggerType = LifetimeActionTriggerType.TIME_AFTER_CREATE;
            this.triggerPeriod = timeAfterCreate;
        } else {
            this.triggerType = LifetimeActionTriggerType.TIME_BEFORE_EXPIRY;
            this.triggerPeriod = timeBeforeExpiry;
        }
    }

    public KeyLifetimeActionTriggerModel(final LifetimeActionTrigger trigger) {
        Assert.notNull(trigger, "Trigger cannot be null.");
        this.triggerType = trigger.triggerType();
        this.triggerPeriod = trigger.timePeriod();
    }

    @JsonGetter
    public @Nullable Period getTimeBeforeExpiry() {
        Period period = null;
        if (triggerType == LifetimeActionTriggerType.TIME_BEFORE_EXPIRY) {
            period = triggerPeriod;
        }
        return period;
    }

    @JsonGetter
    public @Nullable Period getTimeAfterCreate() {
        Period period = null;
        if (triggerType == LifetimeActionTriggerType.TIME_AFTER_CREATE) {
            period = triggerPeriod;
        }
        return period;
    }
}
