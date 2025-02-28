package com.github.nagyesta.lowkeyvault.mapper.v7_3.certificate;

import com.github.nagyesta.lowkeyvault.context.ApiVersionAware;
import com.github.nagyesta.lowkeyvault.mapper.common.AliasAwareConverter;
import com.github.nagyesta.lowkeyvault.model.v7_3.certificate.*;
import com.github.nagyesta.lowkeyvault.service.certificate.ReadOnlyKeyVaultCertificateEntity;
import com.github.nagyesta.lowkeyvault.service.certificate.impl.ReadOnlyCertificatePolicy;
import lombok.NonNull;

import java.net.URI;
import java.util.SortedSet;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class BaseCertificateEntityToV73PolicyModelConverter
        implements AliasAwareConverter<ReadOnlyKeyVaultCertificateEntity, CertificatePolicyModel> {

    private final Supplier<CertificatePolicyModel> modelSupplier;
    private final Function<ReadOnlyKeyVaultCertificateEntity, ReadOnlyCertificatePolicy> policyExtractor;

    protected BaseCertificateEntityToV73PolicyModelConverter(
            @org.springframework.lang.NonNull
            final Function<ReadOnlyKeyVaultCertificateEntity, ReadOnlyCertificatePolicy> policyExtractor) {
        this(CertificatePolicyModel::new, policyExtractor);
    }

    protected BaseCertificateEntityToV73PolicyModelConverter(
            @NonNull final Supplier<CertificatePolicyModel> modelSupplier,
            @NonNull final Function<ReadOnlyKeyVaultCertificateEntity, ReadOnlyCertificatePolicy> policyExtractor) {
        this.modelSupplier = modelSupplier;
        this.policyExtractor = policyExtractor;
    }

    @Override
    @org.springframework.lang.NonNull
    public CertificatePolicyModel convert(
            @org.springframework.lang.NonNull final ReadOnlyKeyVaultCertificateEntity source,
            @org.springframework.lang.NonNull final URI vaultUri) {
        return mapActiveFields(source, modelSupplier.get(), vaultUri);
    }

    protected CertificatePolicyModel mapActiveFields(
            final ReadOnlyKeyVaultCertificateEntity source,
            final CertificatePolicyModel model,
            final URI vaultUri) {
        model.setId(source.getId().asPolicyUri(vaultUri).toString());
        model.setAttributes(convertPolicyProperties(source));
        model.setIssuer(convertIssuer(source));
        model.setKeyProperties(convertKeyProperties(source));
        model.setSecretProperties(convertSecretProperties(source));
        model.setX509Properties(convertX509Properties(source));
        return model;
    }

    private IssuerParameterModel convertIssuer(final ReadOnlyKeyVaultCertificateEntity source) {
        final var issuerParameterModel = new IssuerParameterModel();
        final var policy = policyExtractor.apply(source);
        issuerParameterModel.setIssuer(policy.getCertAuthorityType().getValue());
        issuerParameterModel.setCertType(null);
        issuerParameterModel.setCertTransparency(policy.isEnableTransparency());
        return issuerParameterModel;
    }

    private X509CertificateModel convertX509Properties(final ReadOnlyKeyVaultCertificateEntity source) {
        final var model = new X509CertificateModel();
        final var policy = policyExtractor.apply(source);
        model.setSubject(policy.getSubject());
        model.setKeyUsage(policy.getKeyUsage());
        model.setValidityMonths(policy.getValidityMonths());
        model.setExtendedKeyUsage(policy.getExtendedKeyUsage());
        model.setSubjectAlternativeNames(new SubjectAlternativeNames(policy.getDnsNames(), policy.getEmails(), policy.getUpns()));
        return model;
    }

    private CertificateSecretModel convertSecretProperties(final ReadOnlyKeyVaultCertificateEntity source) {
        final var model = new CertificateSecretModel();
        model.setContentType(policyExtractor.apply(source).getContentType().getMimeType());
        return model;
    }

    private CertificatePropertiesModel convertPolicyProperties(final ReadOnlyKeyVaultCertificateEntity source) {
        final var model = new CertificatePropertiesModel();
        model.setEnabled(source.isEnabled());
        model.setCreatedOn(source.getCreated());
        model.setUpdatedOn(source.getUpdated());
        return model;
    }

    private CertificateKeyModel convertKeyProperties(final ReadOnlyKeyVaultCertificateEntity source) {
        final var policy = policyExtractor.apply(source);
        final var model = new CertificateKeyModel();
        model.setExportable(policy.isExportablePrivateKey());
        model.setReuseKey(policy.isReuseKeyOnRenewal());
        model.setKeySize(policy.getKeySize());
        model.setKeyCurveName(policy.getKeyCurveName());
        model.setKeyType(policy.getKeyType());
        return model;
    }

    @Override
    public SortedSet<String> supportedVersions() {
        return ApiVersionAware.V7_3_AND_LATER;
    }
}
