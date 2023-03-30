package com.github.nagyesta.lowkeyvault.model.v7_3.certificate;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.github.nagyesta.lowkeyvault.model.json.util.Base64CertDeserializer;
import com.github.nagyesta.lowkeyvault.model.json.util.Base64CertSerializer;
import lombok.Data;

@Data
public class KeyVaultPendingCertificateModel {

    @JsonProperty("cancellation_requested")
    private boolean cancellationRequested;

    @JsonProperty("csr")
    @JsonDeserialize(using = Base64CertDeserializer.class)
    @JsonSerialize(using = Base64CertSerializer.class)
    private byte[] csr;

    @JsonProperty("id")
    private String id;

    @JsonProperty("issuer")
    private IssuerParameterModel issuer;

    @JsonProperty("request_id")
    private String requestId;

    @JsonProperty("status")
    private String status;

    @JsonProperty("status_details")
    private String statusDetails;

    @JsonProperty("target")
    private String target;
}
