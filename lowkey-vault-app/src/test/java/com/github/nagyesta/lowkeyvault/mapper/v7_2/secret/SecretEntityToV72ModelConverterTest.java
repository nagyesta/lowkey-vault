package com.github.nagyesta.lowkeyvault.mapper.v7_2.secret;

import com.github.nagyesta.lowkeyvault.model.v7_2.secret.KeyVaultSecretModel;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.net.URI;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

import static com.github.nagyesta.lowkeyvault.TestConstants.*;
import static com.github.nagyesta.lowkeyvault.TestConstantsSecrets.*;
import static com.github.nagyesta.lowkeyvault.TestConstantsUri.HTTPS_LOCALHOST_8443;
import static com.github.nagyesta.lowkeyvault.TestConstantsUri.HTTPS_LOWKEY_VAULT;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.util.MimeTypeUtils.APPLICATION_XML;

class SecretEntityToV72ModelConverterTest {

    @InjectMocks
    private SecretEntityToV72ModelConverter underTest;
    @Mock
    private SecretEntityToV72PropertiesModelConverter propertiesModelConverter;
    @Mock
    private VaultFake vault;
    @Mock
    private SecretVaultFake secretVault;

    private AutoCloseable openMocks;

    public static Stream<Arguments> validInputProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(VERSIONED_SECRET_ENTITY_ID_1_VERSION_1, LOWKEY_VAULT, HTTPS_LOCALHOST_8443, TAGS_EMPTY))
                .add(Arguments.of(VERSIONED_SECRET_ENTITY_ID_1_VERSION_2, LOWKEY_VAULT, HTTPS_LOCALHOST_8443, TAGS_ONE_KEY))
                .add(Arguments.of(VERSIONED_SECRET_ENTITY_ID_1_VERSION_3, LOWKEY_VAULT, HTTPS_LOCALHOST_8443, TAGS_TWO_KEYS))
                .add(Arguments.of(VERSIONED_SECRET_ENTITY_ID_1_VERSION_1, LOWKEY_VAULT, HTTPS_LOCALHOST_8443, TAGS_EMPTY))
                .add(Arguments.of(VERSIONED_SECRET_ENTITY_ID_2_VERSION_1, LOWKEY_VAULT, HTTPS_LOWKEY_VAULT, TAGS_THREE_KEYS))
                .build();
    }

    @BeforeEach
    void setUp() {
        openMocks = MockitoAnnotations.openMocks(this);
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
    void testConvertShouldConvertAllFieldsWhenCalledWithSecretEntity(
            final VersionedSecretEntityId secretEntityId, final String value, final URI baseUri, final Map<String, String> tags) {

        //given
        prepareVaultMock(baseUri);
        final String expectedUri = secretEntityId.asUri(baseUri).toString();
        final KeyVaultSecretEntity input = new KeyVaultSecretEntity(secretEntityId, vault, value, APPLICATION_XML.toString());
        input.setTags(tags);

        //when
        final KeyVaultSecretModel actual = underTest.convert(input, secretEntityId.vault());

        //then
        assertFieldsMatch(tags, input, expectedUri, actual);
        verify(propertiesModelConverter).convert(any(ReadOnlyKeyVaultSecretEntity.class), any(URI.class));
    }

    @ParameterizedTest
    @MethodSource("validInputProvider")
    void testConvertShouldConvertManagedFieldsWhenCalledWithManagedSecretEntity(
            final VersionedSecretEntityId secretEntityId, final String value, final URI baseUri, final Map<String, String> tags) {

        //given
        prepareVaultMock(baseUri);
        final String expectedUri = secretEntityId.asUri(baseUri).toString();
        final KeyVaultSecretEntity input = new KeyVaultSecretEntity(secretEntityId, vault, value, APPLICATION_XML.toString());
        input.setTags(tags);
        input.setManaged(true);

        //when
        final KeyVaultSecretModel actual = underTest.convert(input, secretEntityId.vault());

        //then
        assertFieldsMatch(tags, input, expectedUri, actual);
        Assertions.assertEquals(actual.getId().replaceAll("/secrets/", "/keys/"), actual.getKid());
        verify(propertiesModelConverter).convert(any(ReadOnlyKeyVaultSecretEntity.class), any(URI.class));
    }

    @Test
    void testConstructorShouldThrowExceptionsWhenCalledWithNull() {
        //given

        //when
        //noinspection ConstantConditions
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new SecretEntityToV72ModelConverter(null));

        //then exception
    }

    private void prepareVaultMock(final URI baseUri) {
        when(vault.baseUri()).thenReturn(baseUri);
        when(vault.matches(eq(baseUri))).thenReturn(true);
    }

    private void assertFieldsMatch(final Map<String, String> tags,
                                   final ReadOnlyKeyVaultSecretEntity input,
                                   final String expectedUri,
                                   final KeyVaultSecretModel actual) {
        Assertions.assertIterableEquals(tags.entrySet(), new TreeMap<>(actual.getTags()).entrySet());
        Assertions.assertEquals(SECRET_PROPERTIES_MODEL, actual.getAttributes());
        Assertions.assertEquals(expectedUri, actual.getId());
        Assertions.assertEquals(input.getValue(), actual.getValue());
        Assertions.assertEquals(input.getContentType(), actual.getContentType());
    }
}
