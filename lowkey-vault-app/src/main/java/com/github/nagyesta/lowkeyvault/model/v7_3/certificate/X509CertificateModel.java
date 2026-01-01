package com.github.nagyesta.lowkeyvault.model.v7_3.certificate;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.nagyesta.lowkeyvault.service.certificate.impl.KeyUsageEnum;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.jspecify.annotations.Nullable;

import java.util.Set;

@Data
public class X509CertificateModel {

    @NotNull
    @NotBlank
    @JsonProperty("subject")
    private String subject;
    @Nullable
    @JsonProperty("ekus")
    private Set<String> extendedKeyUsage;
    @Nullable
    @JsonProperty("key_usage")
    private Set<KeyUsageEnum> keyUsage;
    @Nullable
    @JsonProperty("validity_months")
    private Integer validityMonths;
    @Nullable
    @JsonProperty("sans")
    @Valid
    private SubjectAlternativeNames subjectAlternativeNames;

}
