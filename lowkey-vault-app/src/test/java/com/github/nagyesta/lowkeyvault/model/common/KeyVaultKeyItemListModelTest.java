package com.github.nagyesta.lowkeyvault.model.common;

import com.github.nagyesta.lowkeyvault.model.v7_2.key.KeyVaultKeyItemModel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.net.URI;
import java.util.List;
import java.util.stream.Stream;

import static com.github.nagyesta.lowkeyvault.TestConstantsKeys.VERSIONED_KEY_ENTITY_ID_1_VERSION_1;
import static com.github.nagyesta.lowkeyvault.TestConstantsUri.HTTPS_LOCALHOST_8443;

class KeyVaultKeyItemListModelTest {

    public static Stream<Arguments> invalidInputProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(null, null))
                .add(Arguments.of(null, VERSIONED_KEY_ENTITY_ID_1_VERSION_1.asUri(HTTPS_LOCALHOST_8443)))
                .build();
    }

    @ParameterizedTest
    @MethodSource("invalidInputProvider")
    void testConstructorShouldThrowExceptionWhenCalledWithNulls(
            final List<KeyVaultKeyItemModel> list, final URI uri) {
        //given

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> new KeyVaultItemListModel<>(list, uri));

        //then + exception
    }

    @Test
    void testConstructorShouldCreateNewInstanceWhenCalledWithValidData() {
        //given
        final var list = List.of(
                keyVaultKeyItemModel(VERSIONED_KEY_ENTITY_ID_1_VERSION_1.asUri(HTTPS_LOCALHOST_8443)));

        //when
        final var actual = new KeyVaultItemListModel<>(list, null);

        //then
        Assertions.assertNotNull(actual);
        Assertions.assertNull(actual.getNextLink());
        Assertions.assertIterableEquals(list, actual.getValue());
    }

    private KeyVaultKeyItemModel keyVaultKeyItemModel(final URI asUriNoVersion) {
        final var model = new KeyVaultKeyItemModel();
        model.setAttributes(com.github.nagyesta.lowkeyvault.TestConstants.PROPERTIES_MODEL);
        model.setKeyId(asUriNoVersion.toString());
        model.setTags(com.github.nagyesta.lowkeyvault.TestConstants.TAGS_ONE_KEY);
        return model;
    }
}
