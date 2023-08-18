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
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
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
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CertificateGenerator {
    private static final int NUMBER_OF_BITS_SERIAL = 160;
    private final VaultFake vault;
    private final VersionedKeyEntityId kid;
    private final SecureRandom secureRandom = new SecureRandom();

    public CertificateGenerator(@NonNull final VaultFake vault, @NonNull final VersionedKeyEntityId kid) {
        this.vault = vault;
        this.kid = kid;
    }

    public X509Certificate generateCertificate(
            @NonNull final ReadOnlyCertificatePolicy input) throws CryptoException {
        try {
            final ReadOnlyAsymmetricKeyVaultKeyEntity readOnlyKeyVaultKey = vault.keyVaultFake().getEntities()
                    .getEntity(kid, ReadOnlyAsymmetricKeyVaultKeyEntity.class);
            return generateCertificate(input, readOnlyKeyVaultKey.getKey());
        } catch (final Exception e) {
            throw new CryptoException("Failed to generate certificate.", e);
        }
    }

    public PKCS10CertificationRequest generateCertificateSigningRequest(
            @NonNull final String name,
            @NonNull final X509Certificate certificate) throws CryptoException {
        try {
            final ReadOnlyAsymmetricKeyVaultKeyEntity readOnlyKeyVaultKey = vault.keyVaultFake().getEntities()
                    .getEntity(kid, ReadOnlyAsymmetricKeyVaultKeyEntity.class);
            final X500Name subject = generateSubject(certificate.getSubjectX500Principal().getName());
            final KeyPair keyPair = readOnlyKeyVaultKey.getKey();
            final CertificateAlgorithm algorithm = CertificateAlgorithm.forKeyType(readOnlyKeyVaultKey.getKeyType());
            final ContentSigner signer = new JcaContentSignerBuilder(algorithm.getAlgorithm())
                    .setProvider(KeyGenUtil.BOUNCY_CASTLE_PROVIDER)
                    .build(keyPair.getPrivate());
            final PKCS10CertificationRequestBuilder builder = new JcaPKCS10CertificationRequestBuilder(subject, keyPair.getPublic());

            Stream.<ASN1ObjectIdentifier>builder()
                    .add(Extension.subjectAlternativeName)
                    .add(Extension.keyUsage)
                    .add(Extension.extendedKeyUsage)
                    .add(Extension.subjectKeyIdentifier)
                    .add(Extension.authorityKeyIdentifier)
                    .add(Extension.basicConstraints)
                    .build().forEach(e -> addAttributeBasedOnCertificate(builder, certificate, e));

            return builder.build(signer);
        } catch (final Exception e) {
            throw new CryptoException("Failed to generate CSR for certificate with name: " + name, e);
        }
    }

    private X509Certificate generateCertificate(final ReadOnlyCertificatePolicy input, final KeyPair keyPair)
            throws IOException, OperatorCreationException, CertificateException {

        final X509v3CertificateBuilder builder = createCertificateBuilder(input, keyPair);

        addExtensionOptionally(builder, Extension.subjectAlternativeName, false, generateSubjectAlternativeNames(input));
        addExtensionQuietly(builder, Extension.keyUsage, true, generateKeyUsage(input).getEncoded());
        addExtensionOptionally(builder, Extension.extendedKeyUsage, false, convertUsageExtensions(input));

        final X509CertificateHolder holder = buildCertificate(builder, input.getKeyType(), keyPair);

        final JcaX509CertificateConverter converter = new JcaX509CertificateConverter();
        converter.setProvider(KeyGenUtil.BOUNCY_CASTLE_PROVIDER);
        return converter.getCertificate(holder);
    }

    @org.springframework.lang.NonNull
    private KeyUsage generateKeyUsage(final ReadOnlyCertificatePolicy input) {
        return Objects.requireNonNullElse(input.getKeyUsage(), Collections.<KeyUsageEnum>emptyList())
                .stream().collect(KeyUsageEnum.toKeyUsage());
    }

    private ExtendedKeyUsage convertUsageExtensions(final ReadOnlyCertificatePolicy input) {
        ExtendedKeyUsage result = null;
        if (input.getExtendedKeyUsage() != null && !input.getExtendedKeyUsage().isEmpty()) {
            result = new ExtendedKeyUsage(input.getExtendedKeyUsage()
                    .stream()
                    .map(ASN1ObjectIdentifier::new)
                    .map(KeyPurposeId::getInstance)
                    .toList()
                    .toArray(new KeyPurposeId[]{}));
        }
        return result;
    }

    private X509v3CertificateBuilder createCertificateBuilder(
            final ReadOnlyCertificatePolicy input, final KeyPair keyPair) throws IOException {
        final X500Name subject = generateSubject(input.getSubject());
        final X509v3CertificateBuilder certificate = new JcaX509v3CertificateBuilder(
                subject, generateSerial(), input.certNotBefore(), input.certExpiry(), subject, keyPair.getPublic());

        addExtensionOptionally(certificate, Extension.basicConstraints, true, new BasicConstraints(true));
        return certificate;
    }

    private BigInteger generateSerial() {
        return new BigInteger(NUMBER_OF_BITS_SERIAL, secureRandom);
    }

    private X509CertificateHolder buildCertificate(
            final X509v3CertificateBuilder certificate, final KeyType keyType, final KeyPair keyPair)
            throws OperatorCreationException {
        final ContentSigner signer = new JcaContentSignerBuilder(CertificateAlgorithm.forKeyType(keyType).getAlgorithm())
                .setProvider(KeyGenUtil.BOUNCY_CASTLE_PROVIDER)
                .build(keyPair.getPrivate());
        return certificate.build(signer);
    }

    private X500Name generateSubject(final String nameAsString) {
        final RDN[] rdns = IETFUtils.rDNsFromString(nameAsString, BCStyle.INSTANCE);
        final X500NameBuilder x500NameBuilder = new X500NameBuilder(BCStyle.INSTANCE);
        Arrays.stream(rdns).map(RDN::getTypesAndValues).forEach(x500NameBuilder::addMultiValuedRDN);
        return x500NameBuilder.build();
    }

    private GeneralNames generateSubjectAlternativeNames(final ReadOnlyCertificatePolicy input) {
        final GeneralName[] names = generateSubjectAlternativeNamesArray(input);
        GeneralNames result = null;
        if (names.length > 0) {
            result = GeneralNames.getInstance(new DERSequence(names));
        }
        return result;
    }

    private GeneralName[] generateSubjectAlternativeNamesArray(final ReadOnlyCertificatePolicy input) {
        final List<GeneralName> emails = Objects.requireNonNullElse(input.getEmails(), Collections.<String>emptyList()).stream()
                .map(email -> new GeneralName(GeneralName.rfc822Name, email))
                .collect(Collectors.toList());
        final List<GeneralName> dnsNames = Objects.requireNonNullElse(input.getDnsNames(), Collections.<String>emptyList()).stream()
                .map(dns -> new GeneralName(GeneralName.dNSName, dns))
                .collect(Collectors.toList());
        final List<GeneralName> ips = Objects.requireNonNullElse(input.getUpns(), Collections.<String>emptyList()).stream()
                .map(ip -> new GeneralName(GeneralName.iPAddress, ip))
                .collect(Collectors.toList());
        final List<GeneralName> subjectAlternativeNames = Stream.of(emails, dnsNames, ips)
                .flatMap(List::stream)
                .toList();
        return subjectAlternativeNames.toArray(new GeneralName[]{});
    }

    private void addExtensionOptionally(final X509v3CertificateBuilder builder,
                                        final ASN1ObjectIdentifier name,
                                        final boolean isCritical,
                                        final ASN1Encodable value) throws IOException {
            if (value == null) {
                return;
            }
            addExtensionQuietly(builder, name, isCritical, value.toASN1Primitive().getEncoded());
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

    private void addAttributeBasedOnCertificate(
            final PKCS10CertificationRequestBuilder builder,
            final X509Certificate certificate,
            final ASN1ObjectIdentifier extension) {
        addAttributeQuietly(builder, extension, certificate.getCriticalExtensionOIDs().contains(extension.getId()),
                certificate.getExtensionValue(extension.getId()));
    }

    private void addAttributeQuietly(final PKCS10CertificationRequestBuilder builder,
                              final ASN1ObjectIdentifier name,
                              final boolean isCritical,
                              final byte[] value) {
        try {
            if (value == null) {
                return;
            }
            final ExtensionsGenerator generator = new ExtensionsGenerator();
            generator.addExtension(name, isCritical, value);
            builder.addAttribute(PKCSObjectIdentifiers.pkcs_9_at_extensionRequest, generator.generate());
        } catch (final Exception e) {
            throw new CryptoException("Failed to add attribute: " + name, e);
        }
    }
}
