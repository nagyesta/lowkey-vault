package com.github.nagyesta.lowkeyvault.service.certificate.impl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;
import org.bouncycastle.asn1.x509.KeyUsage;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public enum KeyUsageEnum {

    /**
     * Digital signature key usage.
     */
    DIGITAL_SIGNATURE("digitalSignature", KeyUsage.digitalSignature, 0),
    /**
     * Non repudiation key usage.
     */
    NON_REPUDIATION("nonRepudiation", KeyUsage.nonRepudiation, 1),
    /**
     * Key encipherment key usage.
     */
    KEY_ENCIPHERMENT("keyEncipherment", KeyUsage.keyEncipherment, 2),
    /**
     * Data encipherment key usage.
     */
    DATA_ENCIPHERMENT("dataEncipherment", KeyUsage.dataEncipherment, 3),
    /**
     * Key agreement key usage.
     */
    KEY_AGREEMENT("keyAgreement", KeyUsage.keyAgreement, 4),
    /**
     * Key certificate sign key usage.
     */
    KEY_CERT_SIGN("keyCertSign", KeyUsage.keyCertSign, 5),
    /**
     * CRL sign key usage.
     */
    CRL_SIGN("cRLSign", KeyUsage.cRLSign, 6),
    /**
     * Encipher only key usage.
     */
    ENCIPHER_ONLY("encipherOnly", KeyUsage.encipherOnly, 7),
    /**
     * Decipher only key usage.
     */
    DECIPHER_ONLY("decipherOnly", KeyUsage.decipherOnly, 8);

    private final String value;
    private final int code;
    private final int position;

    KeyUsageEnum(final String value, final int code, final int position) {
        this.value = value;
        this.code = code;
        this.position = position;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonIgnore
    public int getCode() {
        return code;
    }

    @JsonIgnore
    public static Set<KeyUsageEnum> parseBitString(final boolean[] usage) {
        final boolean[] bitString = Optional.ofNullable(usage).orElse(new boolean[0]);
        return Arrays.stream(values())
                .filter(e -> bitString.length > e.position && bitString[e.position])
                .collect(Collectors.toSet());
    }

    @JsonCreator
    public static KeyUsageEnum byValue(final String usage) {
        final Optional<KeyUsageEnum> value = Arrays.stream(values())
                .filter(e -> e.value.equals(usage))
                .findFirst();
        return value.orElseThrow(() -> new IllegalArgumentException("Unable to find key usage by value: " + value));
    }

    @JsonIgnore
    public static Collector<KeyUsageEnum, AtomicInteger, KeyUsage> toKeyUsage() {
        return new MergingKeyUsageCollector();
    }

    private static final class MergingKeyUsageCollector implements Collector<KeyUsageEnum, AtomicInteger, KeyUsage> {
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
