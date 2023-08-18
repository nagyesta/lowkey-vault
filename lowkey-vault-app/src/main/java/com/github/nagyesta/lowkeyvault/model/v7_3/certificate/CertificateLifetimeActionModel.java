package com.github.nagyesta.lowkeyvault.model.v7_3.certificate;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.github.nagyesta.lowkeyvault.model.json.util.CertificateLifetimeActionDeserializer;
import com.github.nagyesta.lowkeyvault.model.json.util.CertificateLifetimeActionSerializer;
import com.github.nagyesta.lowkeyvault.service.certificate.CertificateLifetimeActionActivity;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CertificateLifetimeActionModel {

    @Valid
    @NotNull
    @JsonProperty("trigger")
    private CertificateLifetimeActionTriggerModel trigger;

    @Valid
    @NotNull
    @JsonProperty("action")
    @JsonSerialize(using = CertificateLifetimeActionSerializer.class)
    @JsonDeserialize(using = CertificateLifetimeActionDeserializer.class)
    private CertificateLifetimeActionActivity action;
}
