package com.github.nagyesta.lowkeyvault.service.key.util;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERSequence;

import java.io.IOException;
import java.math.BigInteger;

public final class Asn1ConverterUtil {

    private Asn1ConverterUtil() {
        throw new IllegalCallerException("Utility cannot be instantiated.");
    }

    public static byte[] convertFromAsn1toRaw(
            final byte[] signatureAsn1,
            final int paramLength) {
        // Parse ASN.1 encoded signature
        final var sequence = ASN1Sequence.getInstance(signatureAsn1);
        final var rBytes = ((ASN1Integer) sequence.getObjectAt(0)).getValue().toByteArray();
        final var sBytes = ((ASN1Integer) sequence.getObjectAt(1)).getValue().toByteArray();

        // Concatenate the last paramLength bytes of r and s to get the raw RS signature (add 0 bytes if shorter than paramLength)
        final var rawRSSignature = new byte[paramLength * 2];
        mergeInto(rBytes, rawRSSignature, 0, paramLength);
        mergeInto(sBytes, rawRSSignature, paramLength, paramLength);

        // Now rawRSSignature contains the converted raw RS signature
        return rawRSSignature;
    }

    public static byte[] convertFromRawToAsn1(
            final byte[] signatureRaw) throws IOException {
        final var byteLength = signatureRaw.length / 2;
        final var r = new BigInteger(1, signatureRaw, 0, byteLength);
        final var s = new BigInteger(1, signatureRaw, byteLength, byteLength);

        final var sigSequence = new ASN1EncodableVector();
        sigSequence.add(new ASN1Integer(r));
        sigSequence.add(new ASN1Integer(s));
        final var signature = new DERSequence(sigSequence);

        // Output the ASN.1 encoded signature
        return signature.getEncoded();
    }

    private static void mergeInto(
            final byte[] sourceBytes,
            final byte[] destination,
            final int offset,
            final int paramLength) {
        final var copyLength = Math.min(paramLength, sourceBytes.length);
        final var startIndex = Math.max(0, sourceBytes.length - paramLength);
        System.arraycopy(sourceBytes, startIndex, destination, offset + paramLength - copyLength, copyLength);
    }

}
