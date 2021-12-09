package com.github.nagyesta.lowkeyvault.mapper.v7_2.key;

import com.github.nagyesta.lowkeyvault.model.v7_2.key.KeyVaultKeyItemModel;
import com.github.nagyesta.lowkeyvault.service.key.KeyVaultFake;
import com.github.nagyesta.lowkeyvault.service.key.ReadOnlyKeyVaultKeyEntity;
import com.github.nagyesta.lowkeyvault.service.key.id.VersionedKeyEntityId;
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
import java.util.stream.Stream;

import static com.github.nagyesta.lowkeyvault.TestConstants.*;
import static com.github.nagyesta.lowkeyvault.TestConstantsKeys.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class KeyEntityToV72KeyVersionItemModelConverterTest {

    @InjectMocks
    private KeyEntityToV72KeyVersionItemModelConverter underTest;
    @Mock
    private KeyEntityToV72PropertiesModelConverter propertiesModelConverter;
    @Mock
    private VaultFake vault;
    @Mock
    private KeyVaultFake keyVault;

    private AutoCloseable openMocks;

    public static Stream<Arguments> validInputProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(VERSIONED_KEY_ENTITY_ID_1_VERSION_1, MIN_RSA_KEY_SIZE, TAGS_EMPTY,
                        keyVaultKeyItemModel(VERSIONED_KEY_ENTITY_ID_1_VERSION_1.asUri(), TAGS_EMPTY)))
                .add(Arguments.of(VERSIONED_KEY_ENTITY_ID_1_VERSION_2, MIN_RSA_KEY_SIZE, TAGS_ONE_KEY,
                        keyVaultKeyItemModel(VERSIONED_KEY_ENTITY_ID_1_VERSION_2.asUri(), TAGS_ONE_KEY)))
                .add(Arguments.of(VERSIONED_KEY_ENTITY_ID_1_VERSION_3, MIN_RSA_KEY_SIZE, TAGS_TWO_KEYS,
                        keyVaultKeyItemModel(VERSIONED_KEY_ENTITY_ID_1_VERSION_3.asUri(), TAGS_TWO_KEYS)))
                .add(Arguments.of(VERSIONED_KEY_ENTITY_ID_2_VERSION_1, MIN_RSA_KEY_SIZE, TAGS_EMPTY,
                        keyVaultKeyItemModel(VERSIONED_KEY_ENTITY_ID_2_VERSION_1.asUri(), TAGS_EMPTY)))
                .add(Arguments.of(VERSIONED_KEY_ENTITY_ID_2_VERSION_2, MIN_RSA_KEY_SIZE, TAGS_THREE_KEYS,
                        keyVaultKeyItemModel(VERSIONED_KEY_ENTITY_ID_2_VERSION_2.asUri(), TAGS_THREE_KEYS)))
                .build();
    }

    @BeforeEach
    void setUp() {
        openMocks = MockitoAnnotations.openMocks(this);
        when(vault.keyVaultFake()).thenReturn(keyVault);
        when(propertiesModelConverter.convert(any(ReadOnlyKeyVaultKeyEntity.class))).thenReturn(PROPERTIES_MODEL);
    }

    @AfterEach
    void tearDown() throws Exception {
        openMocks.close();
    }

    @ParameterizedTest
    @MethodSource("validInputProvider")
    void testConvertShouldConvertAllFieldsWhenTheyAreSet(
            final VersionedKeyEntityId keyEntityId, final int keyParam, final Map<String, String> tags,
            final KeyVaultKeyItemModel expected) {

        //given
        when(vault.baseUri()).thenReturn(keyEntityId.vault());
        when(vault.matches(eq(keyEntityId.vault()))).thenReturn(true);
        final RsaKeyVaultKeyEntity input = new RsaKeyVaultKeyEntity(keyEntityId, vault, keyParam, null, false);
        input.setTags(tags);

        //when
        final KeyVaultKeyItemModel actual = underTest.convert(input);

        //then
        Assertions.assertEquals(expected, actual);
        verify(propertiesModelConverter).convert(any(ReadOnlyKeyVaultKeyEntity.class));
    }

    @Test
    void testConstructorShouldThrowExceptionsWhenCalledWithNull() {
        //given

        //when
        //noinspection ConstantConditions
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new KeyEntityToV72KeyVersionItemModelConverter(null));

        //then exception
    }

    private static KeyVaultKeyItemModel keyVaultKeyItemModel(final URI asUriNoVersion, final Map<String, String> tags) {
        final KeyVaultKeyItemModel model = new KeyVaultKeyItemModel();
        model.setAttributes(PROPERTIES_MODEL);
        model.setKeyId(asUriNoVersion.toString());
        model.setTags(tags);
        return model;
    }
}
