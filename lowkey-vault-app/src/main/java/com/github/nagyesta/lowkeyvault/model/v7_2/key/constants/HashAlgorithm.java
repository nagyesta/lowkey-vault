package com.github.nagyesta.lowkeyvault.model.v7_2.key.constants;

import lombok.Getter;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DERNull;
import org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.DigestInfo;
import org.springframework.util.Assert;

import java.io.IOException;
import java.lang.reflect.Array;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PSSParameterSpec;
import java.util.Optional;

@SuppressWarnings({"checkstyle:JavadocVariable", "LombokGetterMayBeUsed"})
public enum HashAlgorithm {
    SHA256("SHA-256", 32, NISTObjectIdentifiers.id_sha256),
    SHA384("SHA-384", 48, NISTObjectIdentifiers.id_sha384),
    SHA512("SHA-512", 64, NISTObjectIdentifiers.id_sha512);

    @Getter
    private final String algorithmName;
    private final ASN1ObjectIdentifier algorithmIdentifier;
    private final int digestLength;

    HashAlgorithm(final String algorithmName, final int digestLength, final ASN1ObjectIdentifier algorithmIdentifier) {
        this.algorithmName = algorithmName;
        this.algorithmIdentifier = algorithmIdentifier;
        this.digestLength = digestLength;
    }

    public byte[] encodeDigest(final byte[] digest) throws IOException {
        return new DigestInfo(new AlgorithmIdentifier(algorithmIdentifier, DERNull.INSTANCE), digest).getEncoded();
    }

    public PSSParameterSpec getPssParameter() {
        return new PSSParameterSpec(
                algorithmName,
                "MGF1",
                new MGF1ParameterSpec(algorithmName),
                digestLength,
                PSSParameterSpec.TRAILER_FIELD_BC
        );
    }

    public void verifyDigestLength(final byte[] digest) {
        final int length = Optional.ofNullable(digest)
                .map(Array::getLength)
                .orElseThrow(() -> new IllegalArgumentException("Digest is null."));
        Assert.isTrue(digestLength == length,
                "This algorithm does not support digest length: " + length + ". Expected: " + digestLength);
    }
}
