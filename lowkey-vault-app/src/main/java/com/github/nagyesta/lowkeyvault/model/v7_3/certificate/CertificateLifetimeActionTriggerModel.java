package com.github.nagyesta.lowkeyvault.model.v7_3.certificate;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.nagyesta.lowkeyvault.service.certificate.CertificateLifetimeActionTrigger;
import com.github.nagyesta.lowkeyvault.service.certificate.CertificateLifetimeActionTriggerType;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.Optional;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CertificateLifetimeActionTriggerModel {

    private static final int MONTHLY_LIMIT = 27;
    @Min(1)
    @Max(99)
    @JsonProperty("lifetime_percentage")
    private Integer lifetimePercentage;
    @Min(1)
    @JsonProperty("days_before_expiry")
    private Integer daysBeforeExpiry;

    public CertificateLifetimeActionTriggerModel(final CertificateLifetimeActionTrigger trigger) {
        if (trigger.getTriggerType() == CertificateLifetimeActionTriggerType.DAYS_BEFORE_EXPIRY) {
            this.daysBeforeExpiry = trigger.getValue();
        } else {
            this.lifetimePercentage = trigger.getValue();
        }
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    public void validate(final int validityMonths) {
        Assert.isTrue((lifetimePercentage == null) != (daysBeforeExpiry == null),
                "Exactly one of lifetime_percentage or days_before_expiry must be populated.");
        if (daysBeforeExpiry != null) {
            //bean validation is not active in case of import
            Assert.isTrue(daysBeforeExpiry > 0, "days_before_expiry must be at least 1.");
            Assert.isTrue(daysBeforeExpiry <= MONTHLY_LIMIT * validityMonths,
                    "days_before_expiry must be less or equal than validity_in_months multiplied by 27.");
        } else {
            //bean validation is not active in case of import
            Assert.isTrue(lifetimePercentage > 0, "lifetime_percentage must be at least 1.");
            Assert.isTrue(lifetimePercentage < 100, "lifetime_percentage must be less than 100.");
        }
    }

    public CertificateLifetimeActionTrigger asTriggerEntity() {
        return new CertificateLifetimeActionTrigger(triggerType(), triggerParameter());
    }

    private CertificateLifetimeActionTriggerType triggerType() {
        return Optional.ofNullable(lifetimePercentage)
                .map(v -> CertificateLifetimeActionTriggerType.LIFETIME_PERCENTAGE)
                .orElse(CertificateLifetimeActionTriggerType.DAYS_BEFORE_EXPIRY);
    }

    private int triggerParameter() {
        return Optional.ofNullable(lifetimePercentage)
                .orElse(daysBeforeExpiry);
    }
}
