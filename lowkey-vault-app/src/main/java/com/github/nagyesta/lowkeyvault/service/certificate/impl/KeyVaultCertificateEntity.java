package com.github.nagyesta.lowkeyvault.service.certificate.impl;

import com.github.nagyesta.lowkeyvault.model.v7_3.certificate.CertificateRestoreInput;
import com.github.nagyesta.lowkeyvault.service.EntityId;
import com.github.nagyesta.lowkeyvault.service.certificate.ReadOnlyKeyVaultCertificateEntity;
import com.github.nagyesta.lowkeyvault.service.certificate.id.VersionedCertificateEntityId;
import com.github.nagyesta.lowkeyvault.service.common.BaseVaultEntity;
import com.github.nagyesta.lowkeyvault.service.common.impl.KeyVaultBaseEntity;
import com.github.nagyesta.lowkeyvault.service.exception.CryptoException;
import com.github.nagyesta.lowkeyvault.service.key.id.KeyEntityId;
import com.github.nagyesta.lowkeyvault.service.key.id.VersionedKeyEntityId;
import com.github.nagyesta.lowkeyvault.service.key.impl.KeyVaultKeyEntity;
import com.github.nagyesta.lowkeyvault.service.secret.id.SecretEntityId;
import com.github.nagyesta.lowkeyvault.service.secret.id.VersionedSecretEntityId;
import com.github.nagyesta.lowkeyvault.service.vault.VaultFake;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.springframework.util.Assert;

import java.security.MessageDigest;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;

import static com.github.nagyesta.lowkeyvault.controller.common.util.CertificateRequestMapperUtil.convertPolicyToCertificateCreationInput;

@Slf4j
public class KeyVaultCertificateEntity
        extends KeyVaultBaseEntity<VersionedCertificateEntityId>
        implements ReadOnlyKeyVaultCertificateEntity {

    private final VersionedCertificateEntityId id;
    private final VersionedKeyEntityId kid;
    private final VersionedSecretEntityId sid;
    private final CertificateBackingEntityGenerator generator;
    private X509Certificate certificate;
    private ReadOnlyCertificatePolicy originalCertificatePolicy;
    private final String originalCertificateContents;
    private CertificatePolicy issuancePolicy;
    private PKCS10CertificationRequest csr;

    /**
     * Constructor for certificate creation.
     *
     * @param name  The name of the certificate entity.
     * @param input The input parameters.
     * @param vault The vault we need to use.
     */
    public KeyVaultCertificateEntity(
            @NonNull final String name,
            @NonNull final CertificateCreationInput input,
            @org.springframework.lang.NonNull final VaultFake vault) {
        super(vault);
        Assert.state(name.equals(input.getName()),
                "Certificate name (" + name + ") did not match name from certificate creation input: " + input.getName());
        final var kid = new KeyEntityId(vault.baseUri(), name);
        final var sid = new SecretEntityId(vault.baseUri(), name);
        assertNoNameCollisionWithNotManagedEntity(vault, kid, sid);
        this.issuancePolicy = new CertificatePolicy(input);
        this.originalCertificatePolicy = new CertificatePolicy(input);
        this.generator = new CertificateBackingEntityGenerator(vault);
        this.kid = generator.generateKeyPair(input);
        //reuse the generated key version to produce matching version numbers in all keys
        this.id = new VersionedCertificateEntityId(vault.baseUri(), name, this.kid.version());
        final var certificateGenerator = new CertificateGenerator(vault, this.kid);
        this.certificate = certificateGenerator.generateCertificate(input);
        this.csr = certificateGenerator.generateCertificateSigningRequest(name, this.certificate);
        final var secretEntityId = new VersionedSecretEntityId(vault.baseUri(), input.getName(), this.kid.version());
        this.sid = generator.generateSecret(this.originalCertificatePolicy, this.certificate, this.kid, secretEntityId);
        this.originalCertificateContents = vault.secretVaultFake().getEntities().getReadOnlyEntity(this.sid).getValue();
        normalizeCoreTimeStamps(input, now());
    }


    /**
     * Constructor for certificate import.
     *
     * @param name  The name of the certificate entity.
     * @param input The input parameters.
     * @param vault The vault we need to use.
     */
    public KeyVaultCertificateEntity(
            @NonNull final String name,
            @NonNull final CertificateImportInput input,
            @org.springframework.lang.NonNull final VaultFake vault) {
        super(vault);
        final ReadOnlyCertificatePolicy policy = Optional.ofNullable(input.getCertificateData())
                .orElseThrow(() -> new IllegalArgumentException("Certificate data must not be null."));
        final ReadOnlyCertificatePolicy originalCertificateData = Optional.ofNullable(input.getParsedCertificateData())
                .orElseThrow(() -> new IllegalArgumentException("Parsed certificate data must not be null."));
        final var certificate = Optional.ofNullable(input.getCertificate())
                .orElseThrow(() -> new IllegalArgumentException("Certificate must not be null."));
        final var keyImportRequest = Optional.ofNullable(input.getKeyData())
                .orElseThrow(() -> new IllegalArgumentException("Key data must not be null."));
        Assert.state(name.equals(policy.getName()),
                "Certificate name (" + name + ") did not match name from certificate creation input: " + policy.getName());
        final var kid = new KeyEntityId(vault.baseUri(), name);
        final var sid = new SecretEntityId(vault.baseUri(), name);
        assertNoNameCollisionWithNotManagedEntity(vault, kid, sid);
        this.issuancePolicy = new CertificatePolicy(policy);
        this.originalCertificatePolicy = new CertificatePolicy(originalCertificateData);
        this.generator = new CertificateBackingEntityGenerator(vault);
        this.kid = generator.importKeyPair(policy, keyImportRequest);
        //reuse the generated key version to produce matching version numbers in all keys
        this.id = new VersionedCertificateEntityId(vault.baseUri(), name, this.kid.version());
        final var certificateGenerator = new CertificateGenerator(vault, this.kid);
        this.certificate = certificate;
        this.csr = certificateGenerator.generateCertificateSigningRequest(name, this.certificate);
        final var secretEntityId = new VersionedSecretEntityId(vault.baseUri(), input.getName(), this.kid.version());
        this.sid = generator.generateSecret(this.originalCertificatePolicy, this.certificate, this.kid, secretEntityId);
        this.originalCertificateContents = vault.secretVaultFake().getEntities().getReadOnlyEntity(this.sid).getValue();
        normalizeCoreTimeStamps(policy, now());
    }

    /**
     * Constructor for certificate renewal.
     *
     * @param input The input parameters defining how the certificate should look like.
     * @param kid   The ID of the key entity version we need to use.
     * @param id    The ID of the new certificate entity.
     * @param vault The vault we are using.
     */
    public KeyVaultCertificateEntity(
            @NonNull final ReadOnlyCertificatePolicy input,
            @NonNull final VersionedKeyEntityId kid,
            @NonNull final VersionedCertificateEntityId id,
            @org.springframework.lang.NonNull final VaultFake vault) {
        super(vault);
        Assert.state(vault.keyVaultFake().getEntities().containsEntity(kid),
                "Key must exist to be able to renew certificate using it. " + kid.asUriNoVersion(vault.baseUri()));
        Assert.state(vault.secretVaultFake().getEntities().containsEntityMatching(input.getName(), BaseVaultEntity::isManaged),
                "A version of the Secret must exist to be able to generate a new version using name: " + input.getName());
        this.issuancePolicy = new CertificatePolicy(input);
        this.originalCertificatePolicy = new CertificatePolicy(input);
        this.kid = kid;
        //use the provided id, it might be different from the key id in case the key is reused.
        this.id = id;
        final var certificateGenerator = new CertificateGenerator(vault, this.kid);
        this.certificate = certificateGenerator.generateCertificate(input);
        this.csr = certificateGenerator.generateCertificateSigningRequest(input.getName(), this.certificate);
        final var secretEntityId = new VersionedSecretEntityId(vault.baseUri(), input.getName(), id.version());
        this.generator = new CertificateBackingEntityGenerator(vault);
        this.sid = generator.generateSecret(this.originalCertificatePolicy, this.certificate, this.kid, secretEntityId);
        this.originalCertificateContents = vault.secretVaultFake().getEntities().getReadOnlyEntity(this.sid).getValue();
        normalizeCoreTimeStamps(input, input.getValidityStart());
    }


    /**
     * Constructor for certificate restore.
     *
     * @param id    The id of the certificate entity.
     * @param input The input parameters.
     * @param vault The vault we need to use.
     */
    public KeyVaultCertificateEntity(
            @NonNull final VersionedCertificateEntityId id,
            @NonNull final CertificateRestoreInput input,
            @org.springframework.lang.NonNull final VaultFake vault) {
        super(vault);
        final ReadOnlyCertificatePolicy policy = input.getCertificateData();
        final ReadOnlyCertificatePolicy originalCertificateData = input.getParsedCertificateData();
        final var certificate = input.getCertificate();
        final var keyImportRequest = input.getKeyData();
        final var kid = new VersionedKeyEntityId(vault.baseUri(), id.id(), input.getKeyVersion());
        final var sid = new VersionedSecretEntityId(vault.baseUri(), id.id(), id.version());
        assertNoNameCollisionWithNotManagedEntity(vault, kid, sid);
        this.issuancePolicy = new CertificatePolicy(policy);
        this.originalCertificatePolicy = new CertificatePolicy(originalCertificateData);
        this.generator = new CertificateBackingEntityGenerator(vault);
        if (vault.keyVaultFake().getEntities().containsEntity(kid)) {
            //key already exists, just extend expiry
            this.kid = kid;
            vault.keyVaultFake().getEntities().getEntity(kid, KeyVaultKeyEntity.class).setExpiry(input.getExpires());
        } else {
            this.kid = generator.importKeyPair(kid, policy, keyImportRequest, input.isEnabled());
        }
        this.id = new VersionedCertificateEntityId(vault.baseUri(), id.id(), id.version());
        final var certificateGenerator = new CertificateGenerator(vault, this.kid);
        this.certificate = certificate;
        this.csr = certificateGenerator.generateCertificateSigningRequest(id.id(), this.certificate);
        this.sid = generator.generateSecret(this.originalCertificatePolicy, this.certificate, this.kid, sid);
        this.originalCertificateContents = vault.secretVaultFake().getEntities().getReadOnlyEntity(this.sid).getValue();
        this.updateIssuancePolicy(convertPolicyToCertificateCreationInput(input.getName(), input.getIssuancePolicy()));
        this.setExpiry(input.getExpires());
        this.setEnabled(input.isEnabled());
        this.setTags(input.getTags());
        this.setNotBefore(input.getNotBefore());
        this.setCreatedOn(input.getCreated());
        this.setUpdatedOn(input.getUpdated());
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
    public ReadOnlyCertificatePolicy getIssuancePolicy() {
        return issuancePolicy;
    }

    @Override
    public void updateIssuancePolicy(@NonNull final ReadOnlyCertificatePolicy policy) {
        Assert.isTrue(this.id.id().equals(policy.getName()), "Updated policy must have the same name for: " + this.id);
        this.issuancePolicy = new CertificatePolicy(policy);
        this.updatedNow();
    }

    @Override
    public ReadOnlyCertificatePolicy getOriginalCertificatePolicy() {
        return originalCertificatePolicy;
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
            final var messageDigest = MessageDigest.getInstance("SHA-1");
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
        this.setExpiry(this.getNotBefore().orElse(this.getCreated()).plusMonths(this.originalCertificatePolicy.getValidityMonths()));
    }

    public void regenerateCertificate(final VaultFake vault) {
        if (this.getDeletedDate().isPresent()) {
            log.warn("Deleted certificate is regeneration is skipped for: {}", id);
        } else if (validityStartDateNoLongerAccurate()) {
            log.debug("Regenerating certificate: {}", id);
            final var updated = new CertificatePolicy(originalCertificatePolicy);
            updated.setValidityStart(getCreated());
            regenerateCertificateData(vault, updated);
            generator.updateSecretValueWithNewCertificate(updated, certificate, kid, sid);
        } else {
            log.debug("Validity start date is still accurate certificate won't be changed: {}", id);
        }
    }

    private static void assertNoNameCollisionWithNotManagedEntity(
            final VaultFake vault, final KeyEntityId kid, final SecretEntityId sid) {
        Assert.state(!vault.keyVaultFake().getEntities().containsEntityMatching(kid.id(), KeyVaultCertificateEntity::isNotManaged),
                "Key must not exist to be able to store certificate data in it. " + kid.asUriNoVersion(vault.baseUri()));
        Assert.state(!vault.secretVaultFake().getEntities().containsEntityMatching(sid.id(), KeyVaultCertificateEntity::isNotManaged),
                "Secret must not exist to be able to store certificate data in it. " + sid.asUriNoVersion(vault.baseUri()));
    }

    private static boolean isNotManaged(final BaseVaultEntity<? extends EntityId> e) {
        return !e.isManaged();
    }

    private void normalizeCoreTimeStamps(final ReadOnlyCertificatePolicy certPolicy, final OffsetDateTime createOrUpdate) {
        this.setNotBefore(certPolicy.getValidityStart());
        this.setExpiry(certPolicy.getValidityStart().plusMonths(certPolicy.getValidityMonths()));
        this.setEnabled(true);
        //update timestamps of certificate as the constructor can run for more than a second
        this.setCreatedOn(createOrUpdate);
        this.setUpdatedOn(createOrUpdate);
    }

    private void regenerateCertificateData(final VaultFake vaultFake, final CertificatePolicy updated) {
        final var certificateGenerator = new CertificateGenerator(vaultFake, this.kid);
        this.certificate = certificateGenerator.generateCertificate(updated);
        this.csr = certificateGenerator.generateCertificateSigningRequest(this.id.id(), this.certificate);
        this.originalCertificatePolicy = updated;
    }

    private boolean validityStartDateNoLongerAccurate() {
        final var validityStart = this.getNotBefore().orElse(this.getCreated());
        final var desiredStartOfValidity = Date.from(validityStart.truncatedTo(ChronoUnit.DAYS).toInstant());
        final var existingStartOfValidity = certificate.getNotBefore();
        return !desiredStartOfValidity.equals(existingStartOfValidity);
    }
}
