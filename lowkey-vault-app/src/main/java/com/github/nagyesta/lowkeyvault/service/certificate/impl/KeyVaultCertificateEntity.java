package com.github.nagyesta.lowkeyvault.service.certificate.impl;

import com.github.nagyesta.lowkeyvault.model.v7_2.key.request.JsonWebKeyImportRequest;
import com.github.nagyesta.lowkeyvault.service.certificate.ReadOnlyKeyVaultCertificateEntity;
import com.github.nagyesta.lowkeyvault.service.certificate.id.VersionedCertificateEntityId;
import com.github.nagyesta.lowkeyvault.service.common.impl.KeyVaultBaseEntity;
import com.github.nagyesta.lowkeyvault.service.exception.CryptoException;
import com.github.nagyesta.lowkeyvault.service.key.ReadOnlyAsymmetricKeyVaultKeyEntity;
import com.github.nagyesta.lowkeyvault.service.key.id.KeyEntityId;
import com.github.nagyesta.lowkeyvault.service.key.id.VersionedKeyEntityId;
import com.github.nagyesta.lowkeyvault.service.secret.id.SecretEntityId;
import com.github.nagyesta.lowkeyvault.service.secret.id.VersionedSecretEntityId;
import com.github.nagyesta.lowkeyvault.service.secret.impl.KeyVaultSecretEntity;
import com.github.nagyesta.lowkeyvault.service.vault.VaultFake;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.springframework.util.Assert;

import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;

@Slf4j
public class KeyVaultCertificateEntity
        extends KeyVaultBaseEntity<VersionedCertificateEntityId>
        implements ReadOnlyKeyVaultCertificateEntity {

    private final VersionedCertificateEntityId id;
    private final VersionedKeyEntityId kid;
    private final VersionedSecretEntityId sid;

    private X509Certificate certificate;
    private ReadOnlyCertificatePolicy originalCertificateData;
    private final String originalCertificateContents;
    private final CertificatePolicy policy;
    private PKCS10CertificationRequest csr;

    /**
     * Constructor for certificate creation.
     *
     * @param name  The name of the certificate entity.
     * @param input The input parameters.
     * @param vault The vault we need to use.
     */
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
        this.policy = new CertificatePolicy(input);
        this.kid = generateKeyPair(input, vault);
        //reuse the generated key version to produce matching version numbers in all keys
        this.id = new VersionedCertificateEntityId(vault.baseUri(), name, this.kid.version());
        final CertificateGenerator certificateGenerator = new CertificateGenerator(vault, this.kid);
        this.certificate = certificateGenerator.generateCertificate(input);
        this.csr = certificateGenerator.generateCertificateSigningRequest(name, this.certificate);
        final VersionedSecretEntityId secretEntityId = new VersionedSecretEntityId(vault.baseUri(), input.getName(), this.kid.version());
        this.sid = generateSecret(this.policy, vault, this.certificate, this.kid, secretEntityId);
        this.setNotBefore(input.getValidityStart());
        this.setExpiry(input.getValidityStart().plusMonths(input.getValidityMonths()));
        this.setEnabled(true);
        this.originalCertificateContents = vault.secretVaultFake().getEntities().getReadOnlyEntity(this.sid).getValue();
        this.originalCertificateData = new CertificatePolicy(input);
        //update timestamps of certificate as the constructor can run for more than a second
        this.setCreatedOn(now());
        this.setUpdatedOn(now());
    }


    /**
     * Constructor for certificate import.
     *
     * @param name  The name of the certificate entity.
     * @param input The input parameters.
     * @param vault The vault we need to use.
     */
    public KeyVaultCertificateEntity(@NonNull final String name,
                                     @NonNull final CertificateImportInput input,
                                     @org.springframework.lang.NonNull final VaultFake vault) {
        super(vault);
        final ReadOnlyCertificatePolicy policy = Optional.ofNullable(input.getCertificateData())
                .orElseThrow(() -> new IllegalArgumentException("Certificate data must not be null."));
        final ReadOnlyCertificatePolicy originalCertificateData = Optional.ofNullable(input.getParsedCertificateData())
                .orElseThrow(() -> new IllegalArgumentException("Parsed certificate data must not be null."));
        final X509Certificate certificate = Optional.ofNullable(input.getCertificate())
                .orElseThrow(() -> new IllegalArgumentException("Certificate must not be null."));
        final JsonWebKeyImportRequest keyImportRequest = Optional.ofNullable(input.getKeyData())
                .orElseThrow(() -> new IllegalArgumentException("Key data must not be null."));
        Assert.state(name.equals(policy.getName()),
                "Certificate name (" + name + ") did not match name from certificate creation input: " + policy.getName());
        final KeyEntityId kid = new KeyEntityId(vault.baseUri(), name);
        final SecretEntityId sid = new SecretEntityId(vault.baseUri(), name);
        Assert.state(!vault.keyVaultFake().getEntities().containsName(kid.id()),
                "Key must not exist to be able to store certificate data in it. " + kid.asUriNoVersion(vault.baseUri()));
        Assert.state(!vault.secretVaultFake().getEntities().containsName(sid.id()),
                "Secret must not exist to be able to store certificate data in it. " + sid.asUriNoVersion(vault.baseUri()));
        this.policy = new CertificatePolicy(policy);
        this.kid = importKeyPair(policy, keyImportRequest, vault);
        //reuse the generated key version to produce matching version numbers in all keys
        this.id = new VersionedCertificateEntityId(vault.baseUri(), name, this.kid.version());
        final CertificateGenerator certificateGenerator = new CertificateGenerator(vault, this.kid);
        this.certificate = certificate;
        this.csr = certificateGenerator.generateCertificateSigningRequest(name, this.certificate);
        final VersionedSecretEntityId secretEntityId = new VersionedSecretEntityId(vault.baseUri(), input.getName(), this.kid.version());
        this.sid = generateSecret(this.policy, vault, this.certificate, this.kid, secretEntityId);
        this.setNotBefore(policy.getValidityStart());
        this.setExpiry(policy.getValidityStart().plusMonths(policy.getValidityMonths()));
        this.setEnabled(true);
        this.originalCertificateContents = vault.secretVaultFake().getEntities().getReadOnlyEntity(this.sid).getValue();
        this.originalCertificateData = new CertificatePolicy(originalCertificateData);
        //update timestamps of certificate as the constructor can run for more than a second
        this.setCreatedOn(now());
        this.setUpdatedOn(now());
    }

    /**
     * Constructor for certificate renewal.
     *
     * @param input The input parameters defining how the certificate should look like.
     * @param kid   The Id of the key entity version we need to use.
     * @param id    The Id of the new certificate entity.
     * @param vault The vault we are using.
     */
    public KeyVaultCertificateEntity(@NonNull final ReadOnlyCertificatePolicy input,
                                     @NonNull final VersionedKeyEntityId kid,
                                     @NonNull final VersionedCertificateEntityId id,
                                     @org.springframework.lang.NonNull final VaultFake vault) {
        super(vault);
        Assert.state(vault.keyVaultFake().getEntities().containsEntity(kid),
                "Key must exist to be able to renew certificate using it. " + kid.asUriNoVersion(vault.baseUri()));
        Assert.state(vault.secretVaultFake().getEntities().containsName(input.getName()),
                "A version of the Secret must exist to be able to generate a new version using name: " + input.getName());
        this.policy = new CertificatePolicy(input);
        this.kid = kid;
        //use the provided id, it might be different from the key id in case the key is reused.
        this.id = id;
        final CertificateGenerator certificateGenerator = new CertificateGenerator(vault, this.kid);
        this.certificate = certificateGenerator.generateCertificate(input);
        this.csr = certificateGenerator.generateCertificateSigningRequest(input.getName(), this.certificate);
        final VersionedSecretEntityId secretEntityId = new VersionedSecretEntityId(vault.baseUri(), input.getName(), id.version());
        this.sid = generateSecret(this.policy, vault, this.certificate, this.kid, secretEntityId);
        this.setNotBefore(input.getValidityStart());
        final OffsetDateTime expiry = input.getValidityStart().plusMonths(input.getValidityMonths());
        this.setExpiry(expiry);
        this.setEnabled(true);
        this.originalCertificateContents = vault.secretVaultFake().getEntities().getReadOnlyEntity(this.sid).getValue();
        this.originalCertificateData = new CertificatePolicy(input);
        //update timestamps of certificate
        this.setCreatedOn(input.getValidityStart());
        this.setUpdatedOn(input.getValidityStart());
    }

    private VersionedSecretEntityId generateSecret(final ReadOnlyCertificatePolicy input,
                                                   final VaultFake vault,
                                                   final Certificate certificate,
                                                   final VersionedKeyEntityId kid,
                                                   final VersionedSecretEntityId sid) {
        final KeyPair key = vault.keyVaultFake().getEntities().getEntity(kid, ReadOnlyAsymmetricKeyVaultKeyEntity.class).getKey();
        final String value = input.getContentType().asBase64CertificatePackage(certificate, key);
        final OffsetDateTime start = input.getValidityStart();
        final OffsetDateTime expiry = start.plusMonths(input.getValidityMonths());
        return vault.secretVaultFake().createSecretVersionForCertificate(sid, value, input.getContentType(), start, expiry);
    }

    private VersionedKeyEntityId generateKeyPair(final ReadOnlyCertificatePolicy input, final VaultFake vault) {
        final OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        final OffsetDateTime expiry = now.plusMonths(input.getValidityMonths());
        return vault.keyVaultFake().createKeyVersionForCertificate(input.getName(), input.toKeyCreationInput(), now, expiry);
    }

    private VersionedKeyEntityId importKeyPair(
            final ReadOnlyCertificatePolicy input, final JsonWebKeyImportRequest keyImportRequest, final VaultFake vault) {
        return vault.keyVaultFake().importManagedKeyVersion(input.getName(), keyImportRequest);
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
    public ReadOnlyCertificatePolicy getPolicy() {
        return policy;
    }

    @Override
    public ReadOnlyCertificatePolicy getOriginalCertificateData() {
        return originalCertificateData;
    }

    @Override
    public String getOriginalCertificateContents() {
        return originalCertificateContents;
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

    @Override
    public void timeShift(final int offsetSeconds) {
        super.timeShift(offsetSeconds);
        //reset expiry as it is measured in months while timeShift is using seconds
        //it is better to stay consistent with the behavior of the certificates
        this.setExpiry(this.getNotBefore().orElse(this.getCreated()).plusMonths(this.policy.getValidityMonths()));
    }

    public void regenerateCertificate(final VaultFake vault) {
        if (this.getDeletedDate().isPresent()) {
            log.warn("Deleted certificate is regeneration is skipped for: {}", id);
        } else if (validityStartDateNoLongerAccurate()) {
            log.debug("Regenerating certificate: {}", id);
            final CertificatePolicy updated = new CertificatePolicy(originalCertificateData);
            updated.setValidityStart(getCreated());
            regenerateCertificateData(vault, updated);
            updateSecretValueWithNewCertificate(vault, updated);
        } else {
            log.debug("Validity start date is still accurate certificate won't be changed: {}", id);
        }
    }

    private void updateSecretValueWithNewCertificate(final VaultFake vault, final CertificatePolicy updated) {
        final ReadOnlyAsymmetricKeyVaultKeyEntity key = vault.keyVaultFake().getEntities()
                .getEntity(kid, ReadOnlyAsymmetricKeyVaultKeyEntity.class);
        final KeyVaultSecretEntity secret = vault.secretVaultFake().getEntities().getEntity(this.sid, KeyVaultSecretEntity.class);
        secret.setValue(updated.getContentType().asBase64CertificatePackage(certificate, key.getKey()));
    }

    private void regenerateCertificateData(final VaultFake vaultFake, final CertificatePolicy updated) {
        final CertificateGenerator certificateGenerator = new CertificateGenerator(vaultFake, this.kid);
        this.certificate = certificateGenerator.generateCertificate(updated);
        this.csr = certificateGenerator.generateCertificateSigningRequest(this.id.id(), this.certificate);
        this.originalCertificateData = updated;
    }

    private boolean validityStartDateNoLongerAccurate() {
        final OffsetDateTime validityStart = this.getNotBefore().orElse(this.getCreated());
        final Date desiredStartOfValidity = Date.from(validityStart.truncatedTo(ChronoUnit.DAYS).toInstant());
        final Date existingStartOfValidity = certificate.getNotBefore();
        return !desiredStartOfValidity.equals(existingStartOfValidity);
    }
}
