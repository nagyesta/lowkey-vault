package com.github.nagyesta.lowkeyvault.model.v7_2.secret;

import com.github.nagyesta.lowkeyvault.mapper.common.registry.SecretConverterRegistry;
import com.github.nagyesta.lowkeyvault.mapper.v7_2.secret.SecretEntityToV72BackupConverter;
import com.github.nagyesta.lowkeyvault.mapper.v7_2.secret.SecretEntityToV72PropertiesModelConverter;
import com.github.nagyesta.lowkeyvault.model.common.backup.SecretBackupListItem;
import com.github.nagyesta.lowkeyvault.service.secret.ReadOnlyKeyVaultSecretEntity;
import com.github.nagyesta.lowkeyvault.service.secret.impl.KeyVaultSecretEntity;
import com.github.nagyesta.lowkeyvault.service.vault.VaultFake;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.util.MimeTypeUtils;

import java.net.URI;
import java.util.Collections;
import java.util.Map;

import static com.github.nagyesta.lowkeyvault.TestConstants.*;
import static com.github.nagyesta.lowkeyvault.TestConstantsSecrets.VERSIONED_SECRET_ENTITY_ID_1_VERSION_1;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SecretEntityToV72BackupConverterTest {

    private static final SecretPropertiesModel SECRET_PROPERTIES_MODEL = new SecretPropertiesModel();
    @Mock
    private VaultFake vaultFake;
    @Mock
    private SecretEntityToV72PropertiesModelConverter propertiesModelConverter;
    @Mock
    private SecretConverterRegistry registry;
    @InjectMocks
    private SecretEntityToV72BackupConverter underTest;
    private AutoCloseable openMocks;

    @BeforeEach
    void setUp() {
        openMocks = MockitoAnnotations.openMocks(this);
        when(registry.propertiesConverter(anyString())).thenReturn(propertiesModelConverter);
        when(propertiesModelConverter.convert(any(ReadOnlyKeyVaultSecretEntity.class), any(URI.class)))
                .thenReturn(SECRET_PROPERTIES_MODEL);
    }

    @AfterEach
    void tearDown() throws Exception {
        openMocks.close();
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void testConstructorShouldThrowExceptionWhenCalledWithNull() {
        //given

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> new SecretEntityToV72BackupConverter(null));

        //then + exception
    }

    @Test
    void testConvertShouldConvertPopulatedFieldsWhenCalledWithMinimalInput() {
        //given
        final String value = LOWKEY_VAULT;
        final ReadOnlyKeyVaultSecretEntity input = new KeyVaultSecretEntity(
                VERSIONED_SECRET_ENTITY_ID_1_VERSION_1, vaultFake, value, null);

        //when
        final SecretBackupListItem actual = underTest.convert(input);

        //then
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(value, actual.getValue());
        Assertions.assertNull(actual.getContentType());
        Assertions.assertSame(SECRET_PROPERTIES_MODEL, actual.getAttributes());
        Assertions.assertEquals(Collections.emptyMap(), actual.getTags());
        Assertions.assertFalse(actual.isManaged());
        Assertions.assertEquals(input.getId().vault(), actual.getVaultBaseUri());
        Assertions.assertEquals(input.getId().id(), actual.getId());
        Assertions.assertEquals(input.getId().version(), actual.getVersion());
        verify(propertiesModelConverter).convert(any(ReadOnlyKeyVaultSecretEntity.class), any(URI.class));
        verifyNoMoreInteractions(propertiesModelConverter);
    }

    @Test
    void testConvertShouldConvertAllFieldsWhenCalledWithFullyPopulatedInput() {
        //given
        final String contentType = MimeTypeUtils.TEXT_PLAIN_VALUE;
        final String value = LOWKEY_VAULT;
        final Map<String, String> tagMap = Map.of(KEY_1, VALUE_1);
        final KeyVaultSecretEntity input = new KeyVaultSecretEntity(
                VERSIONED_SECRET_ENTITY_ID_1_VERSION_1, vaultFake, value, contentType);
        input.setTags(tagMap);
        input.setManaged(true);

        //when
        final SecretBackupListItem actual = underTest.convert(input);

        //then
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(value, actual.getValue());
        Assertions.assertEquals(contentType, actual.getContentType());
        Assertions.assertSame(SECRET_PROPERTIES_MODEL, actual.getAttributes());
        Assertions.assertEquals(tagMap, actual.getTags());
        Assertions.assertTrue(actual.isManaged());
        Assertions.assertEquals(input.getId().vault(), actual.getVaultBaseUri());
        Assertions.assertEquals(input.getId().id(), actual.getId());
        Assertions.assertEquals(input.getId().version(), actual.getVersion());
        verify(propertiesModelConverter).convert(any(ReadOnlyKeyVaultSecretEntity.class), any(URI.class));
        verifyNoMoreInteractions(propertiesModelConverter);
    }
}
