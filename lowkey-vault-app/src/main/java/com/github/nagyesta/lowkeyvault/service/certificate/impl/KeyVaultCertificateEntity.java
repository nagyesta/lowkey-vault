package com.github.nagyesta.lowkeyvault.service.certificate.impl;

import com.github.nagyesta.lowkeyvault.service.certificate.ReadOnlyKeyVaultCertificateEntity;
import com.github.nagyesta.lowkeyvault.service.certificate.id.VersionedCertificateEntityId;
import com.github.nagyesta.lowkeyvault.service.common.impl.KeyVaultBaseEntity;
import com.github.nagyesta.lowkeyvault.service.exception.CryptoException;
import com.github.nagyesta.lowkeyvault.service.key.ReadOnlyAsymmetricKeyVaultKeyEntity;
import com.github.nagyesta.lowkeyvault.service.key.id.KeyEntityId;
import com.github.nagyesta.lowkeyvault.service.key.id.VersionedKeyEntityId;
import com.github.nagyesta.lowkeyvault.service.secret.id.SecretEntityId;
import com.github.nagyesta.lowkeyvault.service.secret.id.VersionedSecretEntityId;
import com.github.nagyesta.lowkeyvault.service.vault.VaultFake;
import lombok.NonNull;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.springframework.util.Assert;

import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.cert.Certificate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class KeyVaultCertificateEntity
        extends KeyVaultBaseEntity<VersionedCertificateEntityId>
        implements ReadOnlyKeyVaultCertificateEntity {

    private final VersionedCertificateEntityId id;
    private final VersionedKeyEntityId kid;
    private final VersionedSecretEntityId sid;

    private final Certificate certificate;
    private final CertificateCreationInput generator;
    private final PKCS10CertificationRequest csr;

    public KeyVaultCertificateEntity(@NonNull final String name,
                                     @NonNull final CertificateCreationInput input,
                                     @org.springframework.lang.NonNull final VaultFake vault) {
        super(vault);
        Assert.state(name.equals(input.getName()),
                "Certificate name (" + name + ") did not match name from certificate creation input: " + input.getName());
        final KeyEntityId kid = new KeyEntityId(vault.baseUri(), name);
        final SecretEntityId sid = new SecretEntityId(vault.baseUri(), name);
        Assert.state(!vault.keyVaultFake().getEntities().containsName(kid.id()),
                "Key must not exist to be able to store certificate data in it. " + kid.asUriNoVersion(vault.baseUri()));
        Assert.state(!vault.secretVaultFake().getEntities().containsName(sid.id()),
                "Secret must not exist to be able to store certificate data in it. " + sid.asUriNoVersion(vault.baseUri()));
        this.generator = input;
        this.kid = generateKeyPair(input, vault);
        //reuse the generated key version to produce matching version numbers in all keys
        this.id = new VersionedCertificateEntityId(vault.baseUri(), name, this.kid.version());
        final CertificateGenerator certificateGenerator = new CertificateGenerator(vault, this.kid);
        this.certificate = certificateGenerator.generateCertificate(input);
        this.csr = certificateGenerator.generateCertificateSigningRequest(input);
        this.sid = generateSecret(input, vault, this.certificate, this.kid);
        this.setNotBefore(input.getValidityStart());
        this.setExpiry(input.getValidityStart().plusMonths(input.getValidityMonths()));
        this.setEnabled(true);
    }

    private VersionedSecretEntityId generateSecret(final CertificateCreationInput input,
                                                   final VaultFake vault,
                                                   final Certificate certificate,
                                                   final VersionedKeyEntityId kid) {
        final KeyPair key = vault.keyVaultFake().getEntities().getEntity(kid, ReadOnlyAsymmetricKeyVaultKeyEntity.class).getKey();
        final String value = input.getContentType().asBase64CertificatePackage(certificate, key);
        final OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        final OffsetDateTime expiry = now.plusMonths(input.getValidityMonths());
        final VersionedSecretEntityId secretId = new VersionedSecretEntityId(vault.baseUri(), input.getName(), kid.version());
        return vault.secretVaultFake().createSecretVersionForCertificate(secretId, value, input.getContentType(), now, expiry);
    }

    private VersionedKeyEntityId generateKeyPair(final CertificateCreationInput input, final VaultFake vault) {
        final OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        final OffsetDateTime expiry = now.plusMonths(input.getValidityMonths());
        return vault.keyVaultFake().createKeyVersionForCertificate(input.getName(), input.toKeyCreationInput(), now, expiry);
    }

    @Override
    public VersionedCertificateEntityId getId() {
        return id;
    }

    @Override
    public VersionedKeyEntityId getKid() {
        return kid;
    }

    @Override
    public VersionedSecretEntityId getSid() {
        return sid;
    }

    @Override
    public Certificate getCertificate() {
        return certificate;
    }

    @Override
    public CertificateCreationInput getGenerator() {
        return generator;
    }

    @Override
    public PKCS10CertificationRequest getCertificateSigningRequest() {
        return csr;
    }

    @Override
    public byte[] getThumbprint() throws CryptoException {
        try {
            final MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
            messageDigest.update(getEncodedCertificate());
            return messageDigest.digest();
        } catch (final Exception e) {
            throw new CryptoException("Failed to calculate thumbprint for certificate: " + getId().toString(), e);
        }
    }

    @Override
    public byte[] getEncodedCertificate() throws CryptoException {
        try {
            return getCertificate().getEncoded();
        } catch (final Exception e) {
            throw new CryptoException("Failed to obtain encoded certificate: " + getId().toString(), e);
        }
    }

    @Override
    public byte[] getEncodedCertificateSigningRequest() {
        try {
            byte[] encoded = null;
            if (getCertificateSigningRequest() != null) {
                encoded = getCertificateSigningRequest().getEncoded();
            }
            return encoded;
        } catch (final Exception e) {
            throw new CryptoException("Failed to obtain encoded certificate signing request: " + getId().toString(), e);
        }
    }
}
