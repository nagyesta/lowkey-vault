package com.github.nagyesta.lowkeyvault.mapper.v7_2.secret;

import com.github.nagyesta.lowkeyvault.TestConstants;
import com.github.nagyesta.lowkeyvault.mapper.common.registry.SecretConverterRegistry;
import com.github.nagyesta.lowkeyvault.model.v7_2.secret.DeletedKeyVaultSecretItemModel;
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
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.stream.Stream;

import static com.github.nagyesta.lowkeyvault.TestConstants.*;
import static com.github.nagyesta.lowkeyvault.TestConstantsSecrets.*;
import static com.github.nagyesta.lowkeyvault.TestConstantsUri.HTTPS_LOCALHOST_8443;
import static com.github.nagyesta.lowkeyvault.TestConstantsUri.HTTPS_LOWKEY_VAULT;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SecretEntityToV72SecretItemModelConverterTest {

    private SecretEntityToV72SecretItemModelConverter underTest;
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
                .add(Arguments.of(VERSIONED_SECRET_ENTITY_ID_1_VERSION_1, LOWKEY_VAULT, EMPTY, TAGS_EMPTY,
                        keyVaultSecretItemModel(UNVERSIONED_SECRET_ENTITY_ID_1.asUriNoVersion(HTTPS_LOCALHOST_8443), TAGS_EMPTY)))
                .add(Arguments.of(VERSIONED_SECRET_ENTITY_ID_1_VERSION_2, LOWKEY_VAULT, EMPTY, TAGS_ONE_KEY,
                        keyVaultSecretItemModel(UNVERSIONED_SECRET_ENTITY_ID_1.asUriNoVersion(HTTPS_LOCALHOST_8443), TAGS_ONE_KEY)))
                .add(Arguments.of(VERSIONED_SECRET_ENTITY_ID_1_VERSION_3, LOWKEY_VAULT, EMPTY, TAGS_TWO_KEYS,
                        keyVaultSecretItemModel(UNVERSIONED_SECRET_ENTITY_ID_1.asUriNoVersion(HTTPS_LOCALHOST_8443), TAGS_TWO_KEYS)))
                .add(Arguments.of(VERSIONED_SECRET_ENTITY_ID_2_VERSION_1, LOWKEY_VAULT, EMPTY, TAGS_EMPTY,
                        keyVaultSecretItemModel(UNVERSIONED_SECRET_ENTITY_ID_2.asUriNoVersion(HTTPS_LOWKEY_VAULT), TAGS_EMPTY)))
                .add(Arguments.of(VERSIONED_SECRET_ENTITY_ID_2_VERSION_2, LOWKEY_VAULT, EMPTY, TAGS_THREE_KEYS,
                        keyVaultSecretItemModel(UNVERSIONED_SECRET_ENTITY_ID_2.asUriNoVersion(HTTPS_LOWKEY_VAULT), TAGS_THREE_KEYS)))
                .build();
    }

    public static Stream<Arguments> validDeletedInputProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(VERSIONED_SECRET_ENTITY_ID_1_VERSION_1, LOWKEY_VAULT, EMPTY, TAGS_EMPTY,
                        TIME_10_MINUTES_AGO, TIME_IN_10_MINUTES,
                        deletedKeyVaultSecretItemModel(VERSIONED_SECRET_ENTITY_ID_1_VERSION_1, TAGS_EMPTY,
                                TIME_10_MINUTES_AGO, TIME_IN_10_MINUTES)))
                .add(Arguments.of(VERSIONED_SECRET_ENTITY_ID_1_VERSION_2, LOWKEY_VAULT, EMPTY, TAGS_ONE_KEY,
                        TIME_10_MINUTES_AGO, TIME_IN_10_MINUTES,
                        deletedKeyVaultSecretItemModel(VERSIONED_SECRET_ENTITY_ID_1_VERSION_2, TAGS_ONE_KEY,
                                TIME_10_MINUTES_AGO, TIME_IN_10_MINUTES)))
                .add(Arguments.of(VERSIONED_SECRET_ENTITY_ID_1_VERSION_3, LOWKEY_VAULT, EMPTY, TAGS_TWO_KEYS,
                        NOW, TIME_IN_10_MINUTES,
                        deletedKeyVaultSecretItemModel(VERSIONED_SECRET_ENTITY_ID_1_VERSION_3, TAGS_TWO_KEYS,
                                NOW, TIME_IN_10_MINUTES)))
                .add(Arguments.of(VERSIONED_SECRET_ENTITY_ID_2_VERSION_1, LOWKEY_VAULT, EMPTY, TAGS_EMPTY,
                        TIME_10_MINUTES_AGO, NOW,
                        deletedKeyVaultSecretItemModel(VERSIONED_SECRET_ENTITY_ID_2_VERSION_1, TAGS_EMPTY,
                                TIME_10_MINUTES_AGO, NOW)))
                .add(Arguments.of(VERSIONED_SECRET_ENTITY_ID_2_VERSION_2, LOWKEY_VAULT, EMPTY, TAGS_THREE_KEYS,
                        TIME_10_MINUTES_AGO, TIME_IN_10_MINUTES,
                        deletedKeyVaultSecretItemModel(VERSIONED_SECRET_ENTITY_ID_2_VERSION_2, TAGS_THREE_KEYS,
                                TIME_10_MINUTES_AGO, TIME_IN_10_MINUTES)))
                .build();
    }

    private static KeyVaultSecretItemModel keyVaultSecretItemModel(final URI asUriNoVersion, final Map<String, String> tags) {
        final var model = new KeyVaultSecretItemModel();
        model.setAttributes(TestConstants.SECRET_PROPERTIES_MODEL);
        model.setId(asUriNoVersion.toString());
        model.setTags(tags);
        return model;
    }

    private static DeletedKeyVaultSecretItemModel deletedKeyVaultSecretItemModel(
            final VersionedSecretEntityId id, final Map<String, String> tags,
            final OffsetDateTime deleted, final OffsetDateTime scheduledPurge) {
        final var model = new DeletedKeyVaultSecretItemModel();
        model.setAttributes(TestConstants.SECRET_PROPERTIES_MODEL);
        model.setId(id.asUriNoVersion(id.vault()).toString());
        model.setTags(tags);
        model.setDeletedDate(deleted);
        model.setScheduledPurgeDate(scheduledPurge);
        model.setRecoveryId(id.asRecoveryUri(id.vault()).toString());
        return model;
    }

    @BeforeEach
    void setUp() {
        openMocks = MockitoAnnotations.openMocks(this);
        underTest = new SecretEntityToV72SecretItemModelConverter(registry);
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
            final VersionedSecretEntityId secretEntityId, final String value, final String type, final Map<String, String> tags,
            final KeyVaultSecretItemModel expected) {

        //given
        when(vault.baseUri()).thenReturn(secretEntityId.vault());
        when(vault.matches(secretEntityId.vault(), uri -> uri)).thenReturn(true);
        final var input = new KeyVaultSecretEntity(secretEntityId, vault, value, type);
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
                () -> new SecretEntityToV72SecretItemModelConverter(null));

        //then exception
    }

    @ParameterizedTest
    @MethodSource("validDeletedInputProvider")
    void testConvertDeletedShouldConvertAllFieldsWhenTheyAreSet(
            final VersionedSecretEntityId secretEntityId, final String value, final String type, final Map<String, String> tags,
            final OffsetDateTime deleted, final OffsetDateTime scheduledPurge,
            final DeletedKeyVaultSecretItemModel expected) {

        //given
        when(vault.baseUri()).thenReturn(secretEntityId.vault());
        when(vault.matches(secretEntityId.vault(), uri -> uri)).thenReturn(true);
        final var input = new KeyVaultSecretEntity(secretEntityId, vault, value, type);
        input.setDeletedDate(deleted);
        input.setScheduledPurgeDate(scheduledPurge);
        input.setTags(tags);

        //when
        final var actual = underTest.convertDeleted(input, vault.baseUri());

        //then
        Assertions.assertEquals(expected, actual);
        verify(propertiesModelConverter).convert(any(ReadOnlyKeyVaultSecretEntity.class), any(URI.class));
    }
}
