package com.github.nagyesta.lowkeyvault.steps;

import com.azure.security.keyvault.certificates.models.*;
import com.azure.security.keyvault.keys.cryptography.models.EncryptionAlgorithm;
import com.azure.security.keyvault.keys.cryptography.models.SignatureAlgorithm;
import com.azure.security.keyvault.keys.models.KeyCurveName;
import com.azure.security.keyvault.keys.models.KeyOperation;
import io.cucumber.java.ParameterType;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ParameterTypeDefs {

    private static final String KEY_USAGES =
            "(digitalSignature|nonRepudiation|(key|data)Encipherment|keyAgreement|(keyCert|cRL)Sign|(encipher|decipher)Only)";
    private static final String ENHANCED_KEY_USAGES = "([0-9](\\.[0-9]){8})";

    @ParameterType("(null|(.+:.+)(,.+:.+)*)")
    public Map<String, String> tagMap(final String map) {
        return Optional.ofNullable(map)
                .filter(notTheStringNull())
                .map(s -> s.split(","))
                .map(a -> Arrays.stream(a)
                        .map(kv -> kv.split(":"))
                        .collect(Collectors.toMap(kv -> kv[0], kv -> kv[1]))).orElse(Collections.emptyMap());
    }

    @ParameterType("(null|(encrypt|decrypt|wrapKey|unwrapKey|sign|verify|import)(,(encrypt|decrypt|wrapKey|unwrapKey|sign|verify|import))*)")
    public List<KeyOperation> keyOperations(final String operations) {
        return Optional.ofNullable(operations)
                .filter(notTheStringNull())
                .map(s -> s.split(","))
                .map(a -> Arrays.stream(a)
                        .map(KeyOperation::fromString)
                        .distinct()
                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }

    @ParameterType("(7.2|7.3)")
    public String api(final String api) {
        return api;
    }

    @ParameterType("(P-256|P-256K|P-384|P-521)")
    public KeyCurveName ecCurveName(final String name) {
        return KeyCurveName.fromString(name);
    }

    @ParameterType("(P-256|P-256K|P-384|P-521)")
    public CertificateKeyCurveName ecCertCurveName(final String name) {
        return CertificateKeyCurveName.fromString(name);
    }

    @ParameterType("(PEM|PKCS12)")
    public CertificateContentType certContentType(final String name) {
        return "PEM".equals(name) ? CertificateContentType.PEM : CertificateContentType.PKCS12;
    }

    @ParameterType("(128|192|256)")
    public int octKeySize(final String size) {
        return Integer.parseInt(size);
    }

    @ParameterType("(2048|3072|4096)")
    public int rsaKeySize(final String size) {
        return Integer.parseInt(size);
    }

    @ParameterType("([0-9]{4}-[0-9]{2}-[0-9]{2})")
    public OffsetDateTime expiry(final String date) throws ParseException {
        return OffsetDateTime.parse(date + "T00:00:00+00:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    @ParameterType("[0-9a-zA-Z\\-_]+")
    public String name(final String name) {
        return name;
    }

    @ParameterType("[0-9a-zA-Z\\-_]+\\.[0-9a-zA-Z\\-_]+")
    public String fileName(final String fileName) {
        return fileName;
    }

    @ParameterType("(-|.+)")
    public String password(final String password) {
        if (password.equals("-")) {
            return null;
        }
        return password;
    }

    @ParameterType("(CN=[0-9a-zA-Z\\-_\\.]+)")
    public String subject(final String subject) {
        return subject;
    }

    @ParameterType("[0-9a-zA-Z\\*\\-_\\.,]+")
    public SubjectAlternativeNames sans(final String san) {
        final SubjectAlternativeNames names = new SubjectAlternativeNames();
        final List<String> dnsNames = Arrays.stream(san.split(",")).collect(Collectors.toList());
        names.setDnsNames(dnsNames);
        return names;
    }

    @ParameterType("[0-9a-zA-Z]+/[0-9a-zA-Z]+")
    public String contentType(final String contentType) {
        return contentType;
    }

    @ParameterType(".+")
    public String secretValue(final String secretValue) {
        return secretValue;
    }

    @ParameterType(".+")
    public byte[] clearText(final String value) {
        return value.getBytes(StandardCharsets.UTF_8);
    }

    @ParameterType("(A128CBC|A128CBCPAD|A192CBC|A192CBCPAD|A256CBC|A256CBCPAD|RSA1_5|RSA-OAEP|RSA-OAEP-256)")
    public EncryptionAlgorithm algorithm(final String name) {
        return EncryptionAlgorithm.fromString(name);
    }

    @ParameterType("(ES256|ES256K|ES384|ES512|PS256|PS384|PS512|RS256|RS384|RS512)")
    public SignatureAlgorithm signAlgorithm(final String name) {
        return SignatureAlgorithm.fromString(name);
    }

    @ParameterType("(with|without)")
    public boolean hsm(final String hsm) {
        return "with".equals(hsm);
    }

    @ParameterType("(using|not using)")
    public boolean isUsing(final String using) {
        return "using".equals(using);
    }

    @ParameterType("(percent lifetime|days before expiry)")
    public boolean certLifetimePercentageTrigger(final String trigger) {
        return "percent lifetime".equals(trigger);
    }

    @ParameterType("(EmailContacts|AutoRenew)")
    public CertificatePolicyAction certAction(final String action) {
        return CertificatePolicyAction.fromString(action);
    }

    @ParameterType(KEY_USAGES)
    public CertificateKeyUsage keyUsage(final String rawString) {
        return CertificateKeyUsage.fromString(rawString);
    }

    @ParameterType(ENHANCED_KEY_USAGES)
    public String enhancedKeyUsage(final String rawString) {
        return rawString;
    }

    @ParameterType("(enabled|not enabled)")
    public boolean enabled(final String enabled) {
        return "enabled".equals(enabled);
    }

    @ParameterType("(null|-?[0-9]+)")
    public Integer optionalInt(final String integer) {
        return Optional.ofNullable(integer)
                .filter(notTheStringNull())
                .map(Integer::parseInt)
                .orElse(null);
    }

    private Predicate<String> notTheStringNull() {
        return v -> !v.equals("null");
    }

}
