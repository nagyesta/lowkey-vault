package com.github.nagyesta.lowkeyvault.mapper.v7_2.key;

import com.github.nagyesta.lowkeyvault.TestConstants;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.DeletedKeyVaultKeyItemModel;
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
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.stream.Stream;

import static com.github.nagyesta.lowkeyvault.TestConstants.*;
import static com.github.nagyesta.lowkeyvault.TestConstantsKeys.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class KeyEntityToV72KeyItemModelConverterTest {

    @InjectMocks
    private KeyEntityToV72KeyItemModelConverter underTest;
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
                        keyVaultKeyItemModel(UNVERSIONED_KEY_ENTITY_ID_1.asUriNoVersion(), TAGS_EMPTY)))
                .add(Arguments.of(VERSIONED_KEY_ENTITY_ID_1_VERSION_2, MIN_RSA_KEY_SIZE, TAGS_ONE_KEY,
                        keyVaultKeyItemModel(UNVERSIONED_KEY_ENTITY_ID_1.asUriNoVersion(), TAGS_ONE_KEY)))
                .add(Arguments.of(VERSIONED_KEY_ENTITY_ID_1_VERSION_3, MIN_RSA_KEY_SIZE, TAGS_TWO_KEYS,
                        keyVaultKeyItemModel(UNVERSIONED_KEY_ENTITY_ID_1.asUriNoVersion(), TAGS_TWO_KEYS)))
                .add(Arguments.of(VERSIONED_KEY_ENTITY_ID_2_VERSION_1, MIN_RSA_KEY_SIZE, TAGS_EMPTY,
                        keyVaultKeyItemModel(UNVERSIONED_KEY_ENTITY_ID_2.asUriNoVersion(), TAGS_EMPTY)))
                .add(Arguments.of(VERSIONED_KEY_ENTITY_ID_2_VERSION_2, MIN_RSA_KEY_SIZE, TAGS_THREE_KEYS,
                        keyVaultKeyItemModel(UNVERSIONED_KEY_ENTITY_ID_2.asUriNoVersion(), TAGS_THREE_KEYS)))
                .build();
    }

    public static Stream<Arguments> validDeletedInputProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(VERSIONED_KEY_ENTITY_ID_1_VERSION_1, MIN_RSA_KEY_SIZE, TAGS_EMPTY,
                        TIME_10_MINUTES_AGO, TIME_IN_10_MINUTES,
                        deletedKeyVaultKeyItemModel(VERSIONED_KEY_ENTITY_ID_1_VERSION_1, TAGS_EMPTY,
                                TIME_10_MINUTES_AGO, TIME_IN_10_MINUTES)))
                .add(Arguments.of(VERSIONED_KEY_ENTITY_ID_1_VERSION_2, MIN_RSA_KEY_SIZE, TAGS_ONE_KEY,
                        TIME_10_MINUTES_AGO, TIME_IN_10_MINUTES,
                        deletedKeyVaultKeyItemModel(VERSIONED_KEY_ENTITY_ID_1_VERSION_2, TAGS_ONE_KEY,
                                TIME_10_MINUTES_AGO, TIME_IN_10_MINUTES)))
                .add(Arguments.of(VERSIONED_KEY_ENTITY_ID_1_VERSION_3, MIN_RSA_KEY_SIZE, TAGS_TWO_KEYS,
                        NOW, TIME_IN_10_MINUTES,
                        deletedKeyVaultKeyItemModel(VERSIONED_KEY_ENTITY_ID_1_VERSION_3, TAGS_TWO_KEYS,
                                NOW, TIME_IN_10_MINUTES)))
                .add(Arguments.of(VERSIONED_KEY_ENTITY_ID_2_VERSION_1, MIN_RSA_KEY_SIZE, TAGS_EMPTY,
                        TIME_10_MINUTES_AGO, NOW,
                        deletedKeyVaultKeyItemModel(VERSIONED_KEY_ENTITY_ID_2_VERSION_1, TAGS_EMPTY,
                                TIME_10_MINUTES_AGO, NOW)))
                .add(Arguments.of(VERSIONED_KEY_ENTITY_ID_2_VERSION_2, MIN_RSA_KEY_SIZE, TAGS_THREE_KEYS,
                        TIME_10_MINUTES_AGO, TIME_IN_10_MINUTES,
                        deletedKeyVaultKeyItemModel(VERSIONED_KEY_ENTITY_ID_2_VERSION_2, TAGS_THREE_KEYS,
                                TIME_10_MINUTES_AGO, TIME_IN_10_MINUTES)))
                .build();
    }

    private static KeyVaultKeyItemModel keyVaultKeyItemModel(final URI asUriNoVersion, final Map<String, String> tags) {
        final KeyVaultKeyItemModel model = new KeyVaultKeyItemModel();
        model.setAttributes(TestConstants.PROPERTIES_MODEL);
        model.setKeyId(asUriNoVersion.toString());
        model.setTags(tags);
        return model;
    }

    private static DeletedKeyVaultKeyItemModel deletedKeyVaultKeyItemModel(
            final VersionedKeyEntityId id, final Map<String, String> tags,
            final OffsetDateTime deleted, final OffsetDateTime scheduledPurge) {
        final DeletedKeyVaultKeyItemModel model = new DeletedKeyVaultKeyItemModel();
        model.setAttributes(TestConstants.PROPERTIES_MODEL);
        model.setKeyId(id.asUriNoVersion().toString());
        model.setTags(tags);
        model.setDeletedDate(deleted);
        model.setScheduledPurgeDate(scheduledPurge);
        model.setRecoveryId(id.asRecoveryUri().toString());
        return model;
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
                () -> new KeyEntityToV72KeyItemModelConverter(null));

        //then exception
    }

    @ParameterizedTest
    @MethodSource("validDeletedInputProvider")
    void testConvertDeletedShouldConvertAllFieldsWhenTheyAreSet(
            final VersionedKeyEntityId keyEntityId, final int keyParam, final Map<String, String> tags,
            final OffsetDateTime deleted, final OffsetDateTime scheduledPurge,
            final DeletedKeyVaultKeyItemModel expected) {

        //given
        when(vault.baseUri()).thenReturn(keyEntityId.vault());
        when(vault.matches(eq(keyEntityId.vault()))).thenReturn(true);
        final RsaKeyVaultKeyEntity input = new RsaKeyVaultKeyEntity(keyEntityId, vault, keyParam, null, false);
        input.setDeletedDate(deleted);
        input.setScheduledPurgeDate(scheduledPurge);
        input.setTags(tags);

        //when
        final DeletedKeyVaultKeyItemModel actual = underTest.convertDeleted(input);

        //then
        Assertions.assertEquals(expected, actual);
        verify(propertiesModelConverter).convert(any(ReadOnlyKeyVaultKeyEntity.class));
    }
}
