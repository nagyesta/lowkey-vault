package com.github.nagyesta.lowkeyvault.model.v7_3.certificate;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.nagyesta.lowkeyvault.model.json.util.Base64CertDeserializer;
import com.github.nagyesta.lowkeyvault.model.json.util.Base64CertSerializer;
import lombok.Data;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;

@Data
public class KeyVaultPendingCertificateModel {

    @JsonProperty("cancellation_requested")
    private boolean cancellationRequested;
    @JsonProperty("csr")
    @JsonDeserialize(using = Base64CertDeserializer.class)
    @JsonSerialize(using = Base64CertSerializer.class)
    private byte[] csr;
    @Nullable
    @JsonProperty("id")
    private String id;
    @JsonProperty("issuer")
    private IssuerParameterModel issuer;
    @Nullable
    @JsonProperty("request_id")
    private String requestId;
    @JsonProperty("status")
    private String status;
    @Nullable
    @JsonProperty("status_details")
    private String statusDetails;
    @Nullable
    @JsonProperty("target")
    private String target;

}
