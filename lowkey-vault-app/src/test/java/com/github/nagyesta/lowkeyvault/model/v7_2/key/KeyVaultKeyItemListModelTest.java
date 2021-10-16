package com.github.nagyesta.lowkeyvault.model.v7_2.key;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static com.github.nagyesta.lowkeyvault.TestConstantsKeys.VERSIONED_KEY_ENTITY_ID_1_VERSION_1;

class KeyVaultKeyItemListModelTest {

    public static Stream<Arguments> invalidInputProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(null, null))
                .add(Arguments.of(Collections.emptyList(), null))
                .add(Arguments.of(null, VERSIONED_KEY_ENTITY_ID_1_VERSION_1.asUri()))
                .build();
    }

    @ParameterizedTest
    @MethodSource("invalidInputProvider")
    void testConstructorShouldThrowExceptionWhenCalledWithNulls(
            final List<KeyVaultKeyItemModel> list, final URI uri) {
        //given

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> new KeyVaultKeyItemListModel(list, uri));

        //then + exception
    }

    @Test
    void testConstructorShouldCreateNewInstanceWhenCalledWithValidData() {
        //given
        final List<KeyVaultKeyItemModel> list = List.of(
                keyVaultKeyItemModel(VERSIONED_KEY_ENTITY_ID_1_VERSION_1.asUri()));

        //when
        final KeyVaultKeyItemListModel actual = new KeyVaultKeyItemListModel(list, null);

        //then
        Assertions.assertNotNull(actual);
        Assertions.assertNull(actual.getNextLink());
        Assertions.assertIterableEquals(list, actual.getValue());
    }

    private KeyVaultKeyItemModel keyVaultKeyItemModel(final URI asUriNoVersion) {
        final KeyVaultKeyItemModel model = new KeyVaultKeyItemModel();
        model.setAttributes(com.github.nagyesta.lowkeyvault.TestConstants.PROPERTIES_MODEL);
        model.setKeyId(asUriNoVersion.toString());
        model.setTags(com.github.nagyesta.lowkeyvault.TestConstants.TAGS_ONE_KEY);
        return model;
    }
}
