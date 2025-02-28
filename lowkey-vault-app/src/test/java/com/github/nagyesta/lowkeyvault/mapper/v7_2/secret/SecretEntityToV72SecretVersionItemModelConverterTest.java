package com.github.nagyesta.lowkeyvault.mapper.v7_2.secret;

import com.github.nagyesta.lowkeyvault.mapper.common.registry.SecretConverterRegistry;
import com.github.nagyesta.lowkeyvault.model.v7_2.secret.KeyVaultSecretItemModel;
import com.github.nagyesta.lowkeyvault.service.secret.ReadOnlyKeyVaultSecretEntity;
import com.github.nagyesta.lowkeyvault.service.secret.SecretVaultFake;
import com.github.nagyesta.lowkeyvault.service.secret.id.VersionedSecretEntityId;
import com.github.nagyesta.lowkeyvault.service.secret.impl.KeyVaultSecretEntity;
import com.github.nagyesta.lowkeyvault.service.vault.VaultFake;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.net.URI;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.github.nagyesta.lowkeyvault.TestConstants.*;
import static com.github.nagyesta.lowkeyvault.TestConstantsSecrets.*;
import static com.github.nagyesta.lowkeyvault.TestConstantsUri.HTTPS_LOCALHOST_8443;
import static com.github.nagyesta.lowkeyvault.TestConstantsUri.HTTPS_LOWKEY_VAULT;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SecretEntityToV72SecretVersionItemModelConverterTest {

    private SecretEntityToV72SecretVersionItemModelConverter underTest;
    @Mock
    private SecretEntityToV72PropertiesModelConverter propertiesModelConverter;
    @Mock
    private VaultFake vault;
    @Mock
    private SecretVaultFake secretVault;
    @Mock
    private SecretConverterRegistry registry;

    private AutoCloseable openMocks;

    public static Stream<Arguments> validInputProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(VERSIONED_SECRET_ENTITY_ID_1_VERSION_1, LOCALHOST, TAGS_EMPTY,
                        secretVaultSecretItemModel(VERSIONED_SECRET_ENTITY_ID_1_VERSION_1.asUri(HTTPS_LOCALHOST_8443), TAGS_EMPTY)))
                .add(Arguments.of(VERSIONED_SECRET_ENTITY_ID_1_VERSION_2, LOCALHOST, TAGS_ONE_KEY,
                        secretVaultSecretItemModel(VERSIONED_SECRET_ENTITY_ID_1_VERSION_2.asUri(HTTPS_LOCALHOST_8443), TAGS_ONE_KEY)))
                .add(Arguments.of(VERSIONED_SECRET_ENTITY_ID_1_VERSION_3, LOCALHOST, TAGS_TWO_KEYS,
                        secretVaultSecretItemModel(VERSIONED_SECRET_ENTITY_ID_1_VERSION_3.asUri(HTTPS_LOCALHOST_8443), TAGS_TWO_KEYS)))
                .add(Arguments.of(VERSIONED_SECRET_ENTITY_ID_2_VERSION_1, LOCALHOST, TAGS_EMPTY,
                        secretVaultSecretItemModel(VERSIONED_SECRET_ENTITY_ID_2_VERSION_1.asUri(HTTPS_LOWKEY_VAULT), TAGS_EMPTY)))
                .add(Arguments.of(VERSIONED_SECRET_ENTITY_ID_2_VERSION_2, LOCALHOST, TAGS_THREE_KEYS,
                        secretVaultSecretItemModel(VERSIONED_SECRET_ENTITY_ID_2_VERSION_2.asUri(HTTPS_LOWKEY_VAULT), TAGS_THREE_KEYS)))
                .build();
    }

    private static KeyVaultSecretItemModel secretVaultSecretItemModel(final URI asUriNoVersion, final Map<String, String> tags) {
        final var model = new KeyVaultSecretItemModel();
        model.setAttributes(SECRET_PROPERTIES_MODEL);
        model.setId(asUriNoVersion.toString());
        model.setTags(tags);
        return model;
    }

    @BeforeEach
    void setUp() {
        openMocks = MockitoAnnotations.openMocks(this);
        underTest = new SecretEntityToV72SecretVersionItemModelConverter(registry);
        when(registry.propertiesConverter(anyString())).thenReturn(propertiesModelConverter);
        when(vault.secretVaultFake()).thenReturn(secretVault);
        when(propertiesModelConverter.convert(any(ReadOnlyKeyVaultSecretEntity.class), any(URI.class)))
                .thenReturn(SECRET_PROPERTIES_MODEL);
    }

    @AfterEach
    void tearDown() throws Exception {
        openMocks.close();
    }

    @ParameterizedTest
    @MethodSource("validInputProvider")
    void testConvertShouldConvertAllFieldsWhenTheyAreSet(
            final VersionedSecretEntityId secretEntityId, final String value, final Map<String, String> tags,
            final KeyVaultSecretItemModel expected) {

        //given
        when(vault.baseUri()).thenReturn(secretEntityId.vault());
        final var vaultUri = eq(secretEntityId.vault());
        when(vault.matches(vaultUri, eq(Function.identity()))).thenReturn(true);
        final var input = new KeyVaultSecretEntity(secretEntityId, vault, value, null);
        input.setTags(tags);

        //when
        final var actual = underTest.convert(input, vault.baseUri());

        //then
        Assertions.assertEquals(expected, actual);
        verify(propertiesModelConverter).convert(any(ReadOnlyKeyVaultSecretEntity.class), any(URI.class));
    }

    @Test
    void testConstructorShouldThrowExceptionsWhenCalledWithNull() {
        //given

        //when
        //noinspection ConstantConditions
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new SecretEntityToV72SecretVersionItemModelConverter(null));

        //then exception
    }
}
