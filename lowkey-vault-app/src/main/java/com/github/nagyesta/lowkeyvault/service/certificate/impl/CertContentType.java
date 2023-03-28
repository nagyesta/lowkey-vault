package com.github.nagyesta.lowkeyvault.service.certificate.impl;

import com.github.nagyesta.lowkeyvault.mapper.v7_3.key.EcPrivateKeyToJsonWebKeyImportRequestConverter;
import com.github.nagyesta.lowkeyvault.mapper.v7_3.key.RsaPrivateKeyToJsonWebKeyImportRequestConverter;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyType;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.request.JsonWebKeyImportRequest;
import com.github.nagyesta.lowkeyvault.service.exception.CryptoException;
import com.github.nagyesta.lowkeyvault.service.key.util.KeyGenUtil;
import lombok.NonNull;
import org.apache.tomcat.util.codec.binary.Base64;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.openssl.jcajce.JcaPKCS8Generator;
import org.springframework.util.Assert;
import org.springframework.util.Base64Utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Arrays;
import java.util.List;

import static com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyType.EC;
import static com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyType.RSA;

public enum CertContentType {

    /**
     * Certificate in PKCS12 format.
     */
    PKCS12("application/x-pkcs12") {
        private static final String KEY_STORE_TYPE_PKCS12 = "PKCS12";

        @Override
        public List<Certificate> getCertificateChain(@NonNull final String certificateContent, @NonNull final String password) {
            try {
                final KeyStore pkcs12 = loadKeyStore(certificateContent, password);
                final String alias = pkcs12.aliases().asIterator().next();
                return List.of(pkcs12.getCertificate(alias));
            } catch (final Exception e) {
                throw new CryptoException("Failed to extract key from PKCS12", e);
            }
        }

        @Override
        public JsonWebKeyImportRequest getKey(@NonNull final String certificateContent,
                                              @NonNull final String password) {
            try {
                final KeyStore pkcs12 = loadKeyStore(certificateContent, password);
                final String alias = pkcs12.aliases().asIterator().next();
                final Key parsedKey = pkcs12.getKey(alias, "".toCharArray());
                if (parsedKey instanceof RSAPrivateCrtKey) {
                    return RSA_KEY_CONVERTER.convert((RSAPrivateCrtKey) parsedKey);
                } else {
                    return EC_KEY_CONVERTER.convert((BCECPrivateKey) parsedKey);
                }
            } catch (final Exception e) {
                throw new CryptoException("Failed to extract key from PKCS12", e);
            }
        }

        @Override
        public String asBase64CertificatePackage(@NonNull final Certificate certificate,
                                                 @NonNull final KeyPair keyPair) throws CryptoException {
            final byte[] bytes = generateCertificatePackage(certificate, keyPair, DEFAULT_PASSWORD);
            return Base64Utils.encodeToString(bytes);
        }

        @Override
        public byte[] certificatePackageForBackup(@NonNull final Certificate certificate,
                                                  @NonNull final KeyPair keyPair) throws CryptoException {
            return generateCertificatePackage(certificate, keyPair, BACKUP_PASSWORD);
        }
        private byte[] generateCertificatePackage(
                final Certificate certificate, final KeyPair keyPair, final String password) {
            try {
                final KeyStore pkcs12 = KeyStore.getInstance(KEY_STORE_TYPE_PKCS12);
                pkcs12.load(null, password.toCharArray());
                pkcs12.setKeyEntry(DEFAULT_ALIAS, keyPair.getPrivate(), password.toCharArray(), new Certificate[]{certificate});
                final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                pkcs12.store(byteArrayOutputStream, password.toCharArray());
                return byteArrayOutputStream.toByteArray();
            } catch (final Exception e) {
                throw new CryptoException("Failed to generate PKCS12 certificate.", e);
            }
        }

        private KeyStore loadKeyStore(final String certificateContent, final String password)
                throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
            final KeyStore pkcs12 = KeyStore.getInstance(KEY_STORE_TYPE_PKCS12, KeyGenUtil.BOUNCY_CASTLE_PROVIDER);
            pkcs12.load(new ByteArrayInputStream(Base64.decodeBase64(certificateContent)), password.toCharArray());
            return pkcs12;
        }
    },
    /**
     * Certificate in PEM format.
     */
    PEM("application/x-pem-file") {
        @Override
        public List<Certificate> getCertificateChain(@NonNull final String certificateContent, final String password) {
            try {
                validatePem(certificateContent);
                final byte[] encodedCertificate = extractByteArray(certificateContent,
                        "-----BEGIN CERTIFICATE-----", "-----END CERTIFICATE-----");
                final CertificateFactory fact = CertificateFactory.getInstance("X.509", KeyGenUtil.BOUNCY_CASTLE_PROVIDER);
                return List.of(fact.generateCertificate(new ByteArrayInputStream(encodedCertificate)));
            } catch (final Exception e) {
                throw new CryptoException("Failed to extract key from PEM", e);
            }
        }

        @Override
        public JsonWebKeyImportRequest getKey(@NonNull final String certificateContent,
                                              final String password) {
            try {
                validatePem(certificateContent);
                final byte[] encodedKey = extractByteArray(certificateContent,
                        "-----BEGIN PRIVATE KEY-----", "-----END PRIVATE KEY-----");
                final KeyType keyType = assumeKeyType(encodedKey.length);
                final KeyFactory kf = KeyFactory.getInstance(keyType.getAlgorithmName(), KeyGenUtil.BOUNCY_CASTLE_PROVIDER);
                final PKCS8EncodedKeySpec privSpec = new PKCS8EncodedKeySpec(encodedKey);
                if (RSA == keyType) {
                    return RSA_KEY_CONVERTER.convert((RSAPrivateCrtKey) kf.generatePrivate(privSpec));
                } else {
                    return EC_KEY_CONVERTER.convert((BCECPrivateKey) kf.generatePrivate(privSpec));
                }
            } catch (final Exception e) {
                throw new CryptoException("Failed to extract key from PEM", e);
            }
        }

        @Override
        public String asBase64CertificatePackage(@NonNull final Certificate certificate,
                                                 @NonNull final KeyPair keyPair) throws CryptoException {
            final String key = toPemString(keyPair.getPrivate());
            final String cert = toPemString(certificate);
            return key + cert;
        }

        @Override
        public byte[] certificatePackageForBackup(
                @org.springframework.lang.NonNull final Certificate certificate,
                @org.springframework.lang.NonNull final KeyPair keyPair) throws CryptoException {
            return asBase64CertificatePackage(certificate, keyPair).getBytes(StandardCharsets.UTF_8);
        }

        private void validatePem(final String certificateContent) {
            Assert.isTrue(certificateContent.startsWith(BEGIN), "PEM should start with '-----BEGIN'");
        }

        private String toPemString(final Object object) {
            //noinspection LocalCanBeFinal
            try (StringWriter stringWriter = new StringWriter();
                 JcaPEMWriter pemWriter = new JcaPEMWriter(stringWriter)) {
                if (object instanceof PrivateKey) {
                    pemWriter.writeObject(new JcaPKCS8Generator((PrivateKey) object, null));
                } else {
                    pemWriter.writeObject(object);
                }
                pemWriter.close();
                return stringWriter.toString().replaceAll(ANYTHING_BEFORE_BEGIN, BEGIN);
            } catch (final IOException e) {
                throw new CryptoException("Failed to convert to PEM.", e);
            }
        }

        private byte[] extractByteArray(final String certificateContent, final String beginPattern, final String endPattern) {
            final String withoutNewLines = certificateContent.replaceAll("[\n\r]+", "");
            final String keyOnly = withoutNewLines.replaceAll(".*" + beginPattern, "")
                    .replaceAll(endPattern + ".*", "");
            return Base64.decodeBase64(keyOnly);
        }
    };

    /**
     * Default alias used for the generated PKCS12 certificates.
     */
    public static final String DEFAULT_ALIAS = "certificate";
    /**
     * Default password used for the generated certificates.
     */
    public static final String DEFAULT_PASSWORD = "";
    /**
     * Password used for the backup process.
     */
    public static final String BACKUP_PASSWORD = "export-password";
    private static final String BEGIN = "-----BEGIN";
    private static final String ANYTHING_BEFORE_BEGIN = "^.*-----BEGIN";
    private static final int EC_RSA_KEY_SIZE_THRESHOLD = 150;

    private static KeyType assumeKeyType(final int size) {
        final KeyType keyType;
        if (size < EC_RSA_KEY_SIZE_THRESHOLD) {
            keyType = EC;
        } else {
            keyType = RSA;
        }
        return keyType;
    }

    private final String mimeType;

    private static final RsaPrivateKeyToJsonWebKeyImportRequestConverter
            RSA_KEY_CONVERTER = new RsaPrivateKeyToJsonWebKeyImportRequestConverter();
    private static final EcPrivateKeyToJsonWebKeyImportRequestConverter
            EC_KEY_CONVERTER = new EcPrivateKeyToJsonWebKeyImportRequestConverter();

    CertContentType(final String mimeType) {
        this.mimeType = mimeType;
    }

    public String getMimeType() {
        return mimeType;
    }

    public static CertContentType byMimeType(final String mimeType) {
        return Arrays.stream(values())
                .filter(c -> c.getMimeType().equals(mimeType))
                .findFirst()
                .orElse(PEM);
    }

    public abstract List<Certificate> getCertificateChain(String certificateContent, String password);

    public abstract JsonWebKeyImportRequest getKey(String certificateContent, String password) throws CryptoException;

    public abstract String asBase64CertificatePackage(Certificate certificate, KeyPair keyPair) throws CryptoException;

    public abstract byte[] certificatePackageForBackup(Certificate certificate, KeyPair keyPair) throws CryptoException;
}
