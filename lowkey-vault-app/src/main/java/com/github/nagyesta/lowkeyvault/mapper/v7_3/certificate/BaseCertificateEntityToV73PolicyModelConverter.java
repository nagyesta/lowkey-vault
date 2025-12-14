package com.github.nagyesta.lowkeyvault.mapper.v7_3.certificate;

import com.github.nagyesta.lowkeyvault.model.v7_3.certificate.*;
import com.github.nagyesta.lowkeyvault.service.certificate.impl.ReadOnlyCertificatePolicy;
import org.jspecify.annotations.Nullable;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface BaseCertificateEntityToV73PolicyModelConverter {

    @Mapping(target = "issuer", source = "source.certAuthorityType.value")
    @Mapping(target = "certType", expression = "java(null)")
    @Mapping(target = "certTransparency", source = "source.enableTransparency")
    @Nullable IssuerParameterModel convertIssuer(@Nullable ReadOnlyCertificatePolicy source);

    @Mapping(target = "subject", source = "source.subject")
    @Mapping(target = "keyUsage", source = "source.keyUsage")
    @Mapping(target = "validityMonths", source = "source.validityMonths")
    @Mapping(target = "extendedKeyUsage", source = "source.extendedKeyUsage")
    @Mapping(target = "subjectAlternativeNames", source = "source")
    @Nullable X509CertificateModel convertX509Properties(@Nullable ReadOnlyCertificatePolicy source);

    @Nullable SubjectAlternativeNames convertSan(@Nullable ReadOnlyCertificatePolicy policy);

    @Mapping(target = "contentType", source = "source.contentType.mimeType")
    @Nullable CertificateSecretModel convertSecretProperties(@Nullable ReadOnlyCertificatePolicy source);

    @Mapping(target = "exportable", source = "source.exportablePrivateKey")
    @Mapping(target = "reuseKey", source = "source.reuseKeyOnRenewal")
    @Mapping(target = "keySize", source = "source.keySize")
    @Mapping(target = "keyCurveName", source = "source.keyCurveName")
    @Mapping(target = "keyType", source = "source.keyType")
    @Nullable CertificateKeyModel convertKeyProperties(@Nullable ReadOnlyCertificatePolicy source);

}
