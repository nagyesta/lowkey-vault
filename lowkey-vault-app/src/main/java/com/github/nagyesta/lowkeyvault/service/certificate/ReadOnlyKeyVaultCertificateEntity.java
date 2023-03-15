package com.github.nagyesta.lowkeyvault.service.certificate;

import com.github.nagyesta.lowkeyvault.service.certificate.id.VersionedCertificateEntityId;
import com.github.nagyesta.lowkeyvault.service.certificate.impl.CertificatePolicy;
import com.github.nagyesta.lowkeyvault.service.certificate.impl.ReadOnlyCertificatePolicy;
import com.github.nagyesta.lowkeyvault.service.common.BaseVaultEntity;
import com.github.nagyesta.lowkeyvault.service.key.ReadOnlyDeletedEntity;
import com.github.nagyesta.lowkeyvault.service.key.id.VersionedKeyEntityId;
import com.github.nagyesta.lowkeyvault.service.secret.id.VersionedSecretEntityId;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;

import java.security.cert.Certificate;

public interface ReadOnlyKeyVaultCertificateEntity
        extends BaseVaultEntity<VersionedCertificateEntityId>, ReadOnlyDeletedEntity<VersionedCertificateEntityId> {

    VersionedCertificateEntityId getId();

    VersionedKeyEntityId getKid();

    VersionedSecretEntityId getSid();

    Certificate getCertificate();

    ReadOnlyCertificatePolicy getIssuancePolicy();

    CertificatePolicy getMutableIssuancePolicy();

    ReadOnlyCertificatePolicy getOriginalCertificatePolicy();

    String getOriginalCertificateContents();

    PKCS10CertificationRequest getCertificateSigningRequest();

    byte[] getThumbprint();

    byte[] getEncodedCertificate();

    byte[] getEncodedCertificateSigningRequest();
}
