package com.github.nagyesta.lowkeyvault.service.certificate.impl;

import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyType;
import com.github.nagyesta.lowkeyvault.service.exception.CryptoException;
import com.github.nagyesta.lowkeyvault.service.key.ReadOnlyAsymmetricKeyVaultKeyEntity;
import com.github.nagyesta.lowkeyvault.service.key.id.VersionedKeyEntityId;
import com.github.nagyesta.lowkeyvault.service.key.util.KeyGenUtil;
import com.github.nagyesta.lowkeyvault.service.vault.VaultFake;
import lombok.NonNull;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.IETFUtils;
import org.bouncycastle.asn1.x509.*;
import org.bouncycastle.cert.CertIOException;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.PKCS10CertificationRequestBuilder;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;

import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CertificateGenerator {
    private static final int NUMBER_OF_BITS_SERIAL = 160;
    private static final int NUMBER_OF_BYTES_CID = 20;
    private final VaultFake vault;
    private final VersionedKeyEntityId kid;
    private final SecureRandom secureRandom = new SecureRandom();

    public CertificateGenerator(@NonNull final VaultFake vault, @NonNull final VersionedKeyEntityId kid) {
        this.vault = vault;
        this.kid = kid;
    }

    public Certificate generateCertificate(
            @NonNull final CertificateCreationInput input) throws CryptoException {
        try {
            final ReadOnlyAsymmetricKeyVaultKeyEntity readOnlyKeyVaultKey = vault.keyVaultFake().getEntities()
                    .getEntity(kid, ReadOnlyAsymmetricKeyVaultKeyEntity.class);
            return generateCertificate(input, readOnlyKeyVaultKey.getKey());
        } catch (final Exception e) {
            throw new CryptoException("Failed to generate certificate.", e);
        }
    }

    public PKCS10CertificationRequest generateCertificateSigningRequest(
            @NonNull final ReadOnlyCertificatePolicy input) throws CryptoException {
        try {
            final ReadOnlyAsymmetricKeyVaultKeyEntity readOnlyKeyVaultKey = vault.keyVaultFake().getEntities()
                    .getEntity(kid, ReadOnlyAsymmetricKeyVaultKeyEntity.class);
            final X500Name subject = generateSubject(input);
            final KeyPair keyPair = readOnlyKeyVaultKey.getKey();
            final CertificateAlgorithm algorithm = CertificateAlgorithm.forKeyType(readOnlyKeyVaultKey.getKeyType());
            final ContentSigner signer = new JcaContentSignerBuilder(algorithm.getAlgorithm())
                    .setProvider(KeyGenUtil.BOUNCY_CASTLE_PROVIDER)
                    .build(keyPair.getPrivate());
            final PKCS10CertificationRequestBuilder builder = new JcaPKCS10CertificationRequestBuilder(subject, keyPair.getPublic());
            return builder.build(signer);
        } catch (final Exception e) {
            throw new CryptoException("Failed to generate CSR for certificate with name: " + input.getName(), e);
        }
    }

    private X509Certificate generateCertificate(final CertificateCreationInput input, final KeyPair keyPair)
            throws IOException, OperatorCreationException, CertificateException {

        final X509v3CertificateBuilder builder = createCertificateBuilder(input, keyPair);

        final KeyUsage usage = Objects.requireNonNullElse(input.getKeyUsage(), Collections.<KeyUsageEnum>emptyList())
                .stream().collect(KeyUsageEnum.toKeyUsage());
        addExtensionQuietly(builder, Extension.keyUsage, false, usage.getEncoded());

        Optional.ofNullable(convertUsageExtensions(input))
                .ifPresent(value -> addExtensionQuietly(builder, Extension.extendedKeyUsage, false, value));
        Optional.ofNullable(generateSubjectAlternativeNames(input))
                .ifPresent(value -> addExtensionQuietly(builder, Extension.subjectAlternativeName, false, value));

        final X509CertificateHolder holder = buildCertificate(builder, input.getKeyType(), keyPair);

        final JcaX509CertificateConverter converter = new JcaX509CertificateConverter();
        converter.setProvider(KeyGenUtil.BOUNCY_CASTLE_PROVIDER);
        return converter.getCertificate(holder);
    }

    private ExtendedKeyUsage convertUsageExtensions(final CertificateCreationInput input) {
        ExtendedKeyUsage result = null;
        if (input.getExtendedKeyUsage() != null && !input.getExtendedKeyUsage().isEmpty()) {
            result = new ExtendedKeyUsage(input.getExtendedKeyUsage()
                    .stream()
                    .map(ASN1ObjectIdentifier::new)
                    .map(KeyPurposeId::getInstance)
                    .collect(Collectors.toList())
                    .toArray(new KeyPurposeId[]{}));
        }
        return result;
    }

    private X509v3CertificateBuilder createCertificateBuilder(final CertificateCreationInput input, final KeyPair keyPair) {
        final X500Name subject = generateSubject(input);
        final X509v3CertificateBuilder certificate = new JcaX509v3CertificateBuilder(
                subject, generateSerial(), input.certNotBefore(), input.certExpiry(), subject, keyPair.getPublic());

        final byte[] cid = generateCid();
        addExtensionQuietly(certificate, Extension.subjectKeyIdentifier, false, cid);
        addExtensionQuietly(certificate, Extension.authorityKeyIdentifier, false, cid);
        addExtensionQuietly(certificate, Extension.basicConstraints, true, new BasicConstraints(true));
        return certificate;
    }

    private BigInteger generateSerial() {
        return new BigInteger(NUMBER_OF_BITS_SERIAL, secureRandom);
    }

    private byte[] generateCid() {
        final byte[] cid = new byte[NUMBER_OF_BYTES_CID];
        secureRandom.nextBytes(cid);
        return cid;
    }

    private X509CertificateHolder buildCertificate(
            final X509v3CertificateBuilder certificate, final KeyType keyType, final KeyPair keyPair)
            throws OperatorCreationException {
        final ContentSigner signer = new JcaContentSignerBuilder(CertificateAlgorithm.forKeyType(keyType).getAlgorithm())
                .setProvider(KeyGenUtil.BOUNCY_CASTLE_PROVIDER)
                .build(keyPair.getPrivate());
        return certificate.build(signer);
    }

    X500Name generateSubject(final ReadOnlyCertificatePolicy input) {
        final RDN[] rdns = IETFUtils.rDNsFromString(input.getSubject(), BCStyle.INSTANCE);
        final X500NameBuilder x500NameBuilder = new X500NameBuilder(BCStyle.INSTANCE);
        Arrays.stream(rdns).map(RDN::getTypesAndValues).forEach(x500NameBuilder::addMultiValuedRDN);
        return x500NameBuilder.build();
    }

    GeneralNames generateSubjectAlternativeNames(final CertificateCreationInput input) {
        final List<GeneralName> emails = Objects.requireNonNullElse(input.getEmails(), Collections.<String>emptyList()).stream()
                .map(email -> new GeneralName(GeneralName.rfc822Name, email))
                .collect(Collectors.toList());
        final List<GeneralName> dnsNames = Objects.requireNonNullElse(input.getDnsNames(), Collections.<String>emptyList()).stream()
                .map(dns -> new GeneralName(GeneralName.dNSName, dns))
                .collect(Collectors.toList());
        final List<GeneralName> ips = Objects.requireNonNullElse(input.getIps(), Collections.<String>emptyList()).stream()
                .map(ip -> new GeneralName(GeneralName.iPAddress, ip))
                .collect(Collectors.toList());
        final List<GeneralName> subjectAlternativeNames = Stream.of(emails, dnsNames, ips)
                .flatMap(List::stream)
                .collect(Collectors.toList());
        GeneralNames result = null;
        if (!subjectAlternativeNames.isEmpty()) {
            result = GeneralNames.getInstance(new DERSequence(subjectAlternativeNames.toArray(new GeneralName[]{})));
        }
        return result;
    }

    private void addExtensionQuietly(final X509v3CertificateBuilder builder,
                                     final ASN1ObjectIdentifier name,
                                     final boolean isCritical,
                                     final ASN1Encodable value) {
        try {
            addExtensionQuietly(builder, name, isCritical, value.toASN1Primitive().getEncoded());
        } catch (final Exception e) {
            throw new CryptoException("Failed to add extension: " + name, e);
        }
    }

    private void addExtensionQuietly(final X509v3CertificateBuilder builder,
                                     final ASN1ObjectIdentifier name,
                                     final boolean isCritical,
                                     final byte[] value) {
        try {
            builder.addExtension(name, isCritical, value);
        } catch (final CertIOException e) {
            throw new CryptoException("Failed to add extension: " + name, e);
        }
    }
}
