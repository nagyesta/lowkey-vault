package com.github.nagyesta.lowkeyvault.service.certificate.impl;

import org.bouncycastle.asn1.x509.KeyUsage;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

public enum KeyUsageEnum {

    /**
     * Digital signature key usage.
     */
    DIGITAL_SIGNATURE("digitalSignature", KeyUsage.digitalSignature),
    /**
     * Non repudiation key usage.
     */
    NON_REPUDIATION("nonRepudiation", KeyUsage.nonRepudiation),
    /**
     * Key encipherment key usage.
     */
    KEY_ENCIPHERMENT("keyEncipherment", KeyUsage.keyEncipherment),
    /**
     * Data encipherment key usage.
     */
    DATA_ENCIPHERMENT("dataEncipherment", KeyUsage.dataEncipherment),
    /**
     * Key agreement key usage.
     */
    KEY_AGREEMENT("keyAgreement", KeyUsage.keyAgreement),
    /**
     * Key certificate sign key usage.
     */
    KEY_CERT_SIGN("keyCertSign", KeyUsage.keyCertSign),
    /**
     * CRL sign key usage.
     */
    CRL_SIGN("cRLSign", KeyUsage.cRLSign),
    /**
     * Decipher only key usage.
     */
    DECIPHER_ONLY("decipherOnly", KeyUsage.decipherOnly),
    /**
     * Encipher only key usage.
     */
    ENCIPHER_ONLY("encipherOnly", KeyUsage.encipherOnly);

    private final String value;
    private final int code;

    KeyUsageEnum(final String value, final int code) {
        this.value = value;
        this.code = code;
    }

    public String getValue() {
        return value;
    }

    public int getCode() {
        return code;
    }

    public static Collector<KeyUsageEnum, AtomicInteger, KeyUsage> toKeyUsage() {
        return new MergingKeyUsageCollector();
    }

    private static class MergingKeyUsageCollector implements Collector<KeyUsageEnum, AtomicInteger, KeyUsage> {
        @Override
        public Supplier<AtomicInteger> supplier() {
            return AtomicInteger::new;
        }

        @Override
        public BiConsumer<AtomicInteger, KeyUsageEnum> accumulator() {
            return (a, v) -> a.set(a.get() | v.code);
        }

        @Override
        public BinaryOperator<AtomicInteger> combiner() {
            return (a, b) -> new AtomicInteger(a.get() | b.get());
        }

        @Override
        public Function<AtomicInteger, KeyUsage> finisher() {
            return a -> new KeyUsage(a.get());
        }

        @Override
        public Set<Characteristics> characteristics() {
            return Set.of(Characteristics.UNORDERED);
        }
    }
}
