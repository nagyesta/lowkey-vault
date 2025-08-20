package com.github.nagyesta.lowkeyvault.service.certificate.impl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyOperation;
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
    DIGITAL_SIGNATURE("digitalSignature", KeyUsage.digitalSignature, 0, KeyOps.SIGN_VERIFY),
    /**
     * Non-repudiation key usage.
     */
    NON_REPUDIATION("nonRepudiation", KeyUsage.nonRepudiation, 1, KeyOps.SIGN_VERIFY),
    /**
     * Key encipherment key usage.
     */
    KEY_ENCIPHERMENT("keyEncipherment", KeyUsage.keyEncipherment, 2, KeyOps.ENCRYPT_DECRYPT),
    /**
     * Data encipherment key usage.
     */
    DATA_ENCIPHERMENT("dataEncipherment", KeyUsage.dataEncipherment, 3, KeyOps.ENCRYPT_DECRYPT),
    /**
     * Key agreement key usage.
     */
    KEY_AGREEMENT("keyAgreement", KeyUsage.keyAgreement, 4, KeyOps.SIGN_VERIFY),
    /**
     * Key certificate sign key usage.
     */
    KEY_CERT_SIGN("keyCertSign", KeyUsage.keyCertSign, 5, KeyOps.SIGN_VERIFY),
    /**
     * CRL sign key usage.
     */
    CRL_SIGN("cRLSign", KeyUsage.cRLSign, 6, KeyOps.SIGN_VERIFY),
    /**
     * Encipher only key usage.
     */
    ENCIPHER_ONLY("encipherOnly", KeyUsage.encipherOnly, 7, KeyOps.ENCRYPT_DECRYPT),
    /**
     * Decipher only key usage.
     */
    DECIPHER_ONLY("decipherOnly", KeyUsage.decipherOnly, 8, KeyOps.ENCRYPT_DECRYPT);

    private final String value;
    private final int code;
    private final int position;
    private final Set<KeyOperation> keyOperations;

    KeyUsageEnum(
            final String value,
            final int code,
            final int position,
            final Set<KeyOperation> keyOperations) {
        this.value = value;
        this.code = code;
        this.position = position;
        this.keyOperations = Set.copyOf(keyOperations);
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
    public Set<KeyOperation> getKeyOperations() {
        return keyOperations;
    }

    @JsonIgnore
    public static Set<KeyUsageEnum> parseBitString(final boolean[] usage) {
        final var bitString = Optional.ofNullable(usage).orElse(new boolean[0]);
        return Arrays.stream(values())
                .filter(e -> bitString.length > e.position && bitString[e.position])
                .collect(Collectors.toSet());
    }

    @JsonCreator
    public static KeyUsageEnum byValue(final String usage) {
        final var value = Arrays.stream(values())
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

    private static final class KeyOps {
        /**
         * Collects the key operations related to digital signatures and their verification.
         */
        public static final Set<KeyOperation>
                SIGN_VERIFY = Set.of(KeyOperation.SIGN, KeyOperation.VERIFY);
        /**
         * Collects the key operations related to encryption and decryption.
         */
        public static final Set<KeyOperation>
                ENCRYPT_DECRYPT = Set.of(KeyOperation.ENCRYPT, KeyOperation.DECRYPT, KeyOperation.WRAP_KEY, KeyOperation.UNWRAP_KEY);
    }
}
