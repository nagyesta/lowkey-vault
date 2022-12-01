package com.github.nagyesta.lowkeyvault.model.v7_3.certificate;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.nagyesta.lowkeyvault.service.certificate.impl.KeyUsageEnum;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Set;

@Data
public class X509CertificateModel {

    @NotNull
    @NotBlank
    @JsonProperty("subject")
    private String subject;

    @JsonProperty("ekus")
    private Set<String> extendedKeyUsage;

    @JsonProperty("key_usage")
    private Set<KeyUsageEnum> keyUsage;

    @JsonProperty("validity_months")
    private Integer validityMonths;

    @JsonProperty("sans")
    @Valid
    private SubjectAlternativeNames subjectAlternativeNames;
}
