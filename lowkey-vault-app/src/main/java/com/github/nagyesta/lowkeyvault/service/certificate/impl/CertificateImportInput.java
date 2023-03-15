package com.github.nagyesta.lowkeyvault.service.certificate.impl;

import com.github.nagyesta.lowkeyvault.model.v7_2.key.request.JsonWebKeyImportRequest;
import com.github.nagyesta.lowkeyvault.model.v7_3.certificate.CertificateKeyModel;
import com.github.nagyesta.lowkeyvault.model.v7_3.certificate.CertificateLifetimeActionModel;
import com.github.nagyesta.lowkeyvault.model.v7_3.certificate.CertificatePolicyModel;
import com.github.nagyesta.lowkeyvault.model.v7_3.certificate.IssuerParameterModel;
import com.github.nagyesta.lowkeyvault.service.certificate.impl.CertificateCreationInput.CertificateCreationInputBuilder;
import com.github.nagyesta.lowkeyvault.service.certificate.util.ParserUtil;
import lombok.Data;
import lombok.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import static com.github.nagyesta.lowkeyvault.service.certificate.CertificateLifetimeActionActivity.EMAIL_CONTACTS;

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
        this.parsedCertificateData = parsedInputBuilder(name, contentType, certificate, keyData, policyModel).build();
        this.certificateData = mergePolicies(this.parsedCertificateData, policyModel);
        final CertAuthorityType certAuthorityType = Optional.ofNullable(policyModel.getIssuer())
                .map(IssuerParameterModel::getIssuer)
                .map(CertAuthorityType::byValue)
                .orElse(CertAuthorityType.UNKNOWN);
        final int validityMonths = certificateData.getValidityMonths();
        Optional.ofNullable(policyModel.getLifetimeActions())
                .ifPresent(actions -> validateLifetimeActionList(validityMonths, actions, certAuthorityType));
    }

    private static void validateLifetimeActionList(
            final int validityMonths, final List<CertificateLifetimeActionModel> actions, final CertAuthorityType certAuthorityType) {
        Assert.isTrue(actions.size() < 2, "Only 0 or 1 lifetime actions are allowed.");
        actions.forEach(a -> validateAction(validityMonths, a, certAuthorityType));
    }

    private static void validateAction(
            final int validityMonths, final CertificateLifetimeActionModel actionModel, final CertAuthorityType certAuthorityType) {
        Assert.isTrue(certAuthorityType == CertAuthorityType.SELF_SIGNED || actionModel.getAction() == EMAIL_CONTACTS,
                "Only EmailContacts action allowed for imported certificates with UNKNOWN issuer.");
        actionModel.getTrigger().validate(validityMonths);
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
        return policy;
    }

    private static Consumer<CertificateKeyModel> overrideKeyProperties(final CertificatePolicy policy) {
        return k -> {
            Optional.ofNullable(k.getKeyType()).ifPresent(policy::setKeyType);
            Optional.ofNullable(k.getKeyCurveName()).ifPresent(policy::setKeyCurveName);
            Optional.ofNullable(k.getKeySize()).ifPresent(policy::setKeySize);
        };
    }

}
