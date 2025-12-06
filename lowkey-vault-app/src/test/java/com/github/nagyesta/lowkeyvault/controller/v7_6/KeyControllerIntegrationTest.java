package com.github.nagyesta.lowkeyvault.controller.v7_6;

import com.github.nagyesta.abortmission.booster.jupiter.annotation.LaunchAbortArmed;
import com.github.nagyesta.lowkeyvault.ResourceUtils;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.EncryptionAlgorithm;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyCurveName;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyType;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.SignatureAlgorithm;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.request.ImportKeyRequest;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.request.JsonWebKeyImportRequest;
import com.github.nagyesta.lowkeyvault.service.key.id.KeyEntityId;
import com.github.nagyesta.lowkeyvault.service.key.impl.AesKeyVaultKeyEntity;
import com.github.nagyesta.lowkeyvault.service.key.impl.EcKeyVaultKeyEntity;
import com.github.nagyesta.lowkeyvault.service.key.impl.RsaKeyVaultKeyEntity;
import com.github.nagyesta.lowkeyvault.service.vault.VaultService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.stream.Stream;

import static com.github.nagyesta.lowkeyvault.TestConstantsUri.getRandomVaultUri;

@LaunchAbortArmed
@SpringBootTest
class KeyControllerIntegrationTest {

    private static final byte[] IV = "_iv-param-value_".getBytes(StandardCharsets.UTF_8);
    private static final int AES_256 = 256;
    private static final int RSA_2048 = 2048;
    private static final String SHA_256 = "SHA-256";

    @Autowired
    private VaultService vaultService;
    @Autowired
    @Qualifier("keyControllerV76")
    private KeyController underTest;
    @Autowired
    private ObjectMapper objectMapper;

    public static Stream<Arguments> invalidHsmProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(false, KeyType.EC_HSM))
                .add(Arguments.of(true, KeyType.EC))
                .build();
    }

    @ParameterizedTest
    @MethodSource("invalidHsmProvider")
    void testImportShouldThrowExceptionWhenCalledWithMisalignedHsmConfiguration(final Boolean hsm, final KeyType keyType) {
        //given
        final var input = new ImportKeyRequest();
        input.setHsm(hsm);
        final var key = new JsonWebKeyImportRequest();
        key.setKeyType(keyType);
        key.setCurveName(KeyCurveName.P_256);
        key.setD(new byte[2]);
        key.setX(new byte[2]);
        key.setY(new byte[2]);
        input.setKey(key);
        final var baseUri = getRandomVaultUri();
        vaultService.create(baseUri);
        final var name = "invalid-ec-name";

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.importKey(name, baseUri, input));

        //then + exception
    }

    @Test
    void testImportRsaShouldUseImportKeyWhenCalledWithValidPayload() {
        //given
        final var resource = "/key/import/rsa-import-valid.json";
        final var input = loadResourceAsObject(resource);
        final var baseUri = getRandomVaultUri();
        vaultService.create(baseUri);
        final var name = "rsa-name";
        final var entities = vaultService
                .findByUri(baseUri)
                .keyVaultFake()
                .getEntities();

        //when
        final var response = underTest.importKey(name, baseUri, input);
        final var id = entities.getLatestVersionOfEntity(new KeyEntityId(baseUri, name));
        final var actual = entities.getEntity(id, RsaKeyVaultKeyEntity.class);

        //then
        Assertions.assertNotNull(response);
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(RSA_2048, actual.getKeySize());
        final var encrypted = actual.encryptBytes(name.getBytes(StandardCharsets.UTF_8), EncryptionAlgorithm.RSA_OAEP_256, null);
        final var decrypted = actual.decryptToBytes(encrypted, EncryptionAlgorithm.RSA_OAEP_256, null);
        Assertions.assertEquals(name, new String(decrypted));
    }

    @Test
    void testImportAesShouldUseImportKeyWhenCalledWithValidPayload() {
        //given
        final var resource = "/key/import/aes-import-valid.json";
        final var input = loadResourceAsObject(resource);
        final var baseUri = getRandomVaultUri();
        vaultService.create(baseUri);
        final var name = "aes-name";
        final var entities = vaultService
                .findByUri(baseUri)
                .keyVaultFake()
                .getEntities();

        //when
        final var response = underTest.importKey(name, baseUri, input);
        final var id = entities.getLatestVersionOfEntity(new KeyEntityId(baseUri, name));
        final var actual = entities.getEntity(id, AesKeyVaultKeyEntity.class);

        //then
        Assertions.assertNotNull(response);
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(AES_256, actual.getKeySize());
        final var encrypted = actual.encryptBytes(name.getBytes(StandardCharsets.UTF_8), EncryptionAlgorithm.A256CBCPAD, IV);
        final var decrypted = actual.decryptToBytes(encrypted, EncryptionAlgorithm.A256CBCPAD, IV);
        Assertions.assertEquals(name, new String(decrypted));
    }

    @Test
    void testImportEcShouldUseImportKeyWhenCalledWithValidPayload() {
        //given
        final var resource = "/key/import/ec-import-valid.json";
        final var input = loadResourceAsObject(resource);
        final var baseUri = getRandomVaultUri();
        vaultService.create(baseUri);
        final var name = "ec-name";
        final var entities = vaultService
                .findByUri(baseUri)
                .keyVaultFake()
                .getEntities();
        final var digest = hash(name.getBytes(StandardCharsets.UTF_8));

        //when
        final var response = underTest.importKey(name, baseUri, input);
        final var id = entities.getLatestVersionOfEntity(new KeyEntityId(baseUri, name));
        final var actual = entities.getEntity(id, EcKeyVaultKeyEntity.class);

        //then
        Assertions.assertNotNull(response);
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(KeyCurveName.P_256, actual.getKeyCurveName());
        final var signature = actual.signBytes(digest, SignatureAlgorithm.ES256);
        final var valid = actual.verifySignedBytes(digest, SignatureAlgorithm.ES256, signature);
        Assertions.assertTrue(valid);
    }

    private ImportKeyRequest loadResourceAsObject(final String resource) {
        final var json = ResourceUtils.loadResourceAsString(resource);
        return objectMapper.readerFor(ImportKeyRequest.class).readValue(json);
    }

    private byte[] hash(final byte[] text) {
        try {
            final var md = MessageDigest.getInstance(SHA_256);
            md.update(text);
            return md.digest();
        } catch (final NoSuchAlgorithmException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }
}
