package com.github.nagyesta.lowkeyvault.mapper.v7_2.key;

import com.github.nagyesta.lowkeyvault.mapper.common.registry.KeyConverterRegistry;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.KeyVaultKeyModel;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyCurveName;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyType;
import com.github.nagyesta.lowkeyvault.service.key.KeyVaultFake;
import com.github.nagyesta.lowkeyvault.service.key.ReadOnlyKeyVaultKeyEntity;
import com.github.nagyesta.lowkeyvault.service.key.id.VersionedKeyEntityId;
import com.github.nagyesta.lowkeyvault.service.key.impl.AesKeyVaultKeyEntity;
import com.github.nagyesta.lowkeyvault.service.key.impl.EcKeyVaultKeyEntity;
import com.github.nagyesta.lowkeyvault.service.key.impl.RsaKeyVaultKeyEntity;
import com.github.nagyesta.lowkeyvault.service.vault.VaultFake;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.net.URI;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.github.nagyesta.lowkeyvault.TestConstants.*;
import static com.github.nagyesta.lowkeyvault.TestConstantsKeys.*;
import static com.github.nagyesta.lowkeyvault.TestConstantsUri.HTTPS_LOCALHOST_8443;
import static com.github.nagyesta.lowkeyvault.TestConstantsUri.HTTPS_LOWKEY_VAULT;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class KeyEntityToV72ModelConverterTest {

    @InjectMocks
    private KeyEntityToV72ModelConverter underTest;
    @Mock
    private KeyEntityToV72PropertiesModelConverter propertiesModelConverter;
    @Mock
    private VaultFake vault;
    @Mock
    private KeyVaultFake keyVault;
    @Mock
    private KeyConverterRegistry registry;

    private AutoCloseable openMocks;

    public static Stream<Arguments> validRsaInputProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(VERSIONED_KEY_ENTITY_ID_1_VERSION_1, MIN_RSA_KEY_SIZE, HTTPS_LOCALHOST_8443, true, TAGS_EMPTY))
                .add(Arguments.of(VERSIONED_KEY_ENTITY_ID_1_VERSION_2, MIN_RSA_KEY_SIZE, HTTPS_LOCALHOST_8443, true, TAGS_ONE_KEY))
                .add(Arguments.of(VERSIONED_KEY_ENTITY_ID_1_VERSION_3, MIN_RSA_KEY_SIZE, HTTPS_LOCALHOST_8443, true, TAGS_TWO_KEYS))
                .add(Arguments.of(VERSIONED_KEY_ENTITY_ID_1_VERSION_1, MIN_RSA_KEY_SIZE, HTTPS_LOCALHOST_8443, false, TAGS_EMPTY))
                .add(Arguments.of(VERSIONED_KEY_ENTITY_ID_2_VERSION_1, MIN_RSA_KEY_SIZE, HTTPS_LOWKEY_VAULT, false, TAGS_THREE_KEYS))
                .build();
    }

    public static Stream<Arguments> validEcInputProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(VERSIONED_KEY_ENTITY_ID_1_VERSION_1, KeyCurveName.P_256, HTTPS_LOCALHOST_8443, true, TAGS_EMPTY))
                .add(Arguments.of(VERSIONED_KEY_ENTITY_ID_1_VERSION_2, KeyCurveName.P_256, HTTPS_LOCALHOST_8443, true, TAGS_ONE_KEY))
                .add(Arguments.of(VERSIONED_KEY_ENTITY_ID_1_VERSION_3, KeyCurveName.P_256, HTTPS_LOCALHOST_8443, true, TAGS_TWO_KEYS))
                .add(Arguments.of(VERSIONED_KEY_ENTITY_ID_1_VERSION_1, KeyCurveName.P_256, HTTPS_LOCALHOST_8443, false, TAGS_EMPTY))
                .add(Arguments.of(VERSIONED_KEY_ENTITY_ID_2_VERSION_1, KeyCurveName.P_256, HTTPS_LOWKEY_VAULT, false, TAGS_THREE_KEYS))
                .build();
    }

    public static Stream<Arguments> validOctInputProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(VERSIONED_KEY_ENTITY_ID_1_VERSION_1, MIN_AES_KEY_SIZE, HTTPS_LOCALHOST_8443, true, TAGS_EMPTY))
                .add(Arguments.of(VERSIONED_KEY_ENTITY_ID_1_VERSION_2, MIN_AES_KEY_SIZE, HTTPS_LOCALHOST_8443, true, TAGS_ONE_KEY))
                .add(Arguments.of(VERSIONED_KEY_ENTITY_ID_1_VERSION_3, MIN_AES_KEY_SIZE, HTTPS_LOCALHOST_8443, true, TAGS_TWO_KEYS))
                .add(Arguments.of(VERSIONED_KEY_ENTITY_ID_1_VERSION_1, MIN_AES_KEY_SIZE, HTTPS_LOCALHOST_8443, true, TAGS_EMPTY))
                .add(Arguments.of(VERSIONED_KEY_ENTITY_ID_2_VERSION_1, MIN_AES_KEY_SIZE, HTTPS_LOWKEY_VAULT, true, TAGS_THREE_KEYS))
                .build();
    }

    @BeforeEach
    void setUp() {
        openMocks = MockitoAnnotations.openMocks(this);
        //Use any as the converter supports both versions and the internal call might use the latest version
        when(registry.propertiesConverter(anyString())).thenReturn(propertiesModelConverter);
        when(registry.versionedEntityId(any(URI.class), anyString(), anyString())).thenCallRealMethod();
        when(registry.entityId(any(URI.class), anyString())).thenCallRealMethod();
        when(vault.keyVaultFake()).thenReturn(keyVault);
        when(propertiesModelConverter.convert(any(ReadOnlyKeyVaultKeyEntity.class), any(URI.class))).thenReturn(PROPERTIES_MODEL);
    }

    @AfterEach
    void tearDown() throws Exception {
        openMocks.close();
    }

    @ParameterizedTest
    @MethodSource("validRsaInputProvider")
    void testConvertShouldConvertAllRsaFieldsWhenCalledWithRsaKeyEntity(
            final VersionedKeyEntityId keyEntityId, final int keyParam, final URI baseUri,
            final boolean hsm, final Map<String, String> tags) {

        //given
        prepareVaultMock(baseUri);
        final String expectedUri = keyEntityId.asUri(baseUri).toString();
        final RsaKeyVaultKeyEntity input = new RsaKeyVaultKeyEntity(keyEntityId, vault, keyParam, null, hsm);
        input.setTags(tags);

        //when
        final KeyVaultKeyModel actual = underTest.convert(input, keyEntityId.vault());

        //then
        assertCommonFieldsMatch(tags, input, expectedUri, actual);
        if (hsm) {
            Assertions.assertEquals(KeyType.RSA_HSM, actual.getKey().getKeyType());
        } else {
            Assertions.assertEquals(KeyType.RSA, actual.getKey().getKeyType());
        }
        Assertions.assertArrayEquals(input.getN(), actual.getKey().getN());
        Assertions.assertArrayEquals(input.getE(), actual.getKey().getE());
        assertEcFieldsAreNull(actual);
        assertOctFieldsAreNull(actual);

        verify(propertiesModelConverter).convert(any(ReadOnlyKeyVaultKeyEntity.class), any(URI.class));
    }

    @ParameterizedTest
    @MethodSource("validEcInputProvider")
    void testConvertShouldConvertAllEcFieldsWhenCalledWithEcKeyEntity(
            final VersionedKeyEntityId keyEntityId, final KeyCurveName keyParam, final URI baseUri,
            final boolean hsm, final Map<String, String> tags) {

        //given
        prepareVaultMock(baseUri);
        final String expectedUri = keyEntityId.asUri(baseUri).toString();
        final EcKeyVaultKeyEntity input = new EcKeyVaultKeyEntity(keyEntityId, vault, keyParam, hsm);
        input.setTags(tags);

        //when
        final KeyVaultKeyModel actual = underTest.convert(input, keyEntityId.vault());

        //then
        assertCommonFieldsMatch(tags, input, expectedUri, actual);
        if (hsm) {
            Assertions.assertEquals(KeyType.EC_HSM, actual.getKey().getKeyType());
        } else {
            Assertions.assertEquals(KeyType.EC, actual.getKey().getKeyType());
        }
        Assertions.assertEquals(input.getKeyCurveName(), actual.getKey().getCurveName());
        Assertions.assertArrayEquals(input.getX(), actual.getKey().getX());
        Assertions.assertArrayEquals(input.getY(), actual.getKey().getY());
        assertRsaFieldsAreNull(actual);
        assertOctFieldsAreNull(actual);

        verify(propertiesModelConverter).convert(any(ReadOnlyKeyVaultKeyEntity.class), any(URI.class));
    }

    @ParameterizedTest
    @MethodSource("validOctInputProvider")
    void testConvertShouldConvertAllOctFieldsWhenCalledWithOctKeyEntity(
            final VersionedKeyEntityId keyEntityId, final int keyParam, final URI baseUri,
            final boolean hsm, final Map<String, String> tags) {

        //given
        prepareVaultMock(baseUri);
        final String expectedUri = keyEntityId.asUri(baseUri).toString();
        final AesKeyVaultKeyEntity input = new AesKeyVaultKeyEntity(keyEntityId, vault, keyParam, hsm);
        input.setTags(tags);

        //when
        final KeyVaultKeyModel actual = underTest.convert(input, keyEntityId.vault());

        //then
        assertCommonFieldsMatch(tags, input, expectedUri, actual);
        if (hsm) {
            Assertions.assertEquals(KeyType.OCT_HSM, actual.getKey().getKeyType());
        } else {
            Assertions.fail("Only HSM is supported, software protection isn't.");
        }
        Assertions.assertNull(actual.getKey().getK());
        assertRsaFieldsAreNull(actual);
        assertEcFieldsAreNull(actual);

    }

    private void assertRsaFieldsAreNull(final KeyVaultKeyModel actual) {
        Assertions.assertNull(actual.getKey().getN());
        Assertions.assertNull(actual.getKey().getE());
    }

    private void assertEcFieldsAreNull(final KeyVaultKeyModel actual) {
        Assertions.assertNull(actual.getKey().getX());
        Assertions.assertNull(actual.getKey().getY());
        Assertions.assertNull(actual.getKey().getCurveName());
    }

    private void assertOctFieldsAreNull(final KeyVaultKeyModel actual) {
        Assertions.assertNull(actual.getKey().getK());
    }

    @Test
    void testConstructorShouldThrowExceptionsWhenCalledWithNull() {
        //given

        //when
        //noinspection ConstantConditions
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new KeyEntityToV72ModelConverter(null));

        //then exception
    }

    private void prepareVaultMock(final URI baseUri) {
        when(vault.baseUri()).thenReturn(baseUri);
        final URI vaultUri = eq(baseUri);
        when(vault.matches(vaultUri, eq(Function.identity()))).thenReturn(true);
    }

    private void assertCommonFieldsMatch(final Map<String, String> tags,
                                         final ReadOnlyKeyVaultKeyEntity input,
                                         final String expectedUri,
                                         final KeyVaultKeyModel actual) {
        Assertions.assertIterableEquals(tags.entrySet(), new TreeMap<>(actual.getTags()).entrySet());
        Assertions.assertEquals(PROPERTIES_MODEL, actual.getAttributes());
        Assertions.assertEquals(expectedUri, actual.getKey().getId());
        Assertions.assertIterableEquals(input.getOperations(), actual.getKey().getKeyOps());
    }
}
