package com.github.nagyesta.lowkeyvault.service.certificate.impl;

import com.github.nagyesta.lowkeyvault.model.v7_2.key.request.JsonWebKeyImportRequest;
import com.github.nagyesta.lowkeyvault.model.v7_3.certificate.*;
import com.github.nagyesta.lowkeyvault.service.certificate.impl.CertificateCreationInput.CertificateCreationInputBuilder;
import com.github.nagyesta.lowkeyvault.service.certificate.util.ParserUtil;
import lombok.Data;
import lombok.NonNull;
import org.springframework.lang.Nullable;

import java.security.cert.X509Certificate;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

@Data
public class CertificateImportInput {

    private final String name;
    private final CertificatePolicy certificateData;
    private final CertificateCreationInput parsedCertificateData;

    private final JsonWebKeyImportRequest keyData;

    private final X509Certificate certificate;

    private final CertificatePolicyModel policyModel;

    public CertificateImportInput(@NonNull final String name,
                                  @NonNull final String certificateContent,
                                  @Nullable final String password,
                                  @NonNull final CertContentType contentType,
                                  @NonNull final CertificatePolicyModel policyModel) {
        this.name = name;
        this.certificate = (X509Certificate) contentType.getCertificateChain(certificateContent, password).get(0);
        this.keyData = contentType.getKey(certificateContent, password);
        this.policyModel = policyModel;
        final CertificateCreationInputBuilder builder = parsedInputBuilder(name, contentType, certificate, keyData, policyModel);
        this.parsedCertificateData = builder.build();
        this.certificateData = mergePolicies(this.parsedCertificateData, policyModel);
    }

    private static CertificateCreationInputBuilder parsedInputBuilder(
            final String name,
            final CertContentType contentType,
            final X509Certificate certificate,
            final JsonWebKeyImportRequest keyData,
            final CertificatePolicyModel policyModel) {
        final CertificateKeyModel keyProperties = Objects.requireNonNullElse(policyModel.getKeyProperties(), new CertificateKeyModel());
        final IssuerParameterModel issuer = Objects.requireNonNullElse(policyModel.getIssuer(), new IssuerParameterModel());
        return ParserUtil.parseCertProperties(certificate)
                .name(name)
                .contentType(contentType)
                .certificateType(issuer.getCertType())
                .enableTransparency(issuer.isCertTransparency())
                .keySize(ParserUtil.findKeySize(keyData))
                .keyCurveName(ParserUtil.findKeyCurve(keyData))
                .keyType(keyData.getKeyType())
                .exportablePrivateKey(keyProperties.isExportable())
                .reuseKeyOnRenewal(keyProperties.isReuseKey());
    }

    private static CertificatePolicy mergePolicies(
            final CertificateCreationInput parsedCertificateData, final CertificatePolicyModel policyModel) {
        final CertificatePolicy policy = new CertificatePolicy(parsedCertificateData);
        Optional.ofNullable(policyModel.getKeyProperties())
                .ifPresent(overrideKeyProperties(policy));
        Optional.ofNullable(policyModel.getX509Properties())
                .ifPresent(overrideX509Properties(policy));
        return policy;
    }

    private static Consumer<CertificateKeyModel> overrideKeyProperties(final CertificatePolicy policy) {
        return k -> {
            Optional.ofNullable(k.getKeyType()).ifPresent(policy::setKeyType);
            Optional.ofNullable(k.getKeyCurveName()).ifPresent(policy::setKeyCurveName);
            Optional.ofNullable(k.getKeySize()).ifPresent(policy::setKeySize);
        };
    }

    private static Consumer<X509CertificateModel> overrideX509Properties(final CertificatePolicy policy) {
        return x -> {
            Optional.ofNullable(x.getSubject()).ifPresent(policy::setSubject);
            final Optional<SubjectAlternativeNames> subjectAlternativeNames = Optional.ofNullable(x.getSubjectAlternativeNames());
            subjectAlternativeNames.map(SubjectAlternativeNames::getDnsNames).ifPresent(policy::setDnsNames);
            subjectAlternativeNames.map(SubjectAlternativeNames::getUpns).ifPresent(policy::setIps);
            subjectAlternativeNames.map(SubjectAlternativeNames::getEmails).ifPresent(policy::setEmails);
            Optional.ofNullable(x.getValidityMonths()).ifPresent(policy::setValidityMonths);
            Optional.ofNullable(x.getKeyUsage()).ifPresent(policy::setKeyUsage);
            Optional.ofNullable(x.getExtendedKeyUsage()).ifPresent(policy::setExtendedKeyUsage);
        };
    }

}
