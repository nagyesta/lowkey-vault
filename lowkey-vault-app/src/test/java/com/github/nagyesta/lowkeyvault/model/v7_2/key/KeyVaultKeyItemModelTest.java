package com.github.nagyesta.lowkeyvault.model.v7_2.key;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.net.URI;
import java.util.Map;
import java.util.stream.Stream;

import static com.github.nagyesta.lowkeyvault.TestConstants.*;
import static com.github.nagyesta.lowkeyvault.TestConstantsKeys.VERSIONED_KEY_ENTITY_ID_1_VERSION_1;

class KeyVaultKeyItemModelTest {

    public static Stream<Arguments> invalidInputProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(null, null, null))
                .add(Arguments.of(PROPERTIES_MODEL, null, null))
                .add(Arguments.of(null, VERSIONED_KEY_ENTITY_ID_1_VERSION_1.asUri(), null))
                .add(Arguments.of(null, null, TAGS_EMPTY))
                .add(Arguments.of(null, VERSIONED_KEY_ENTITY_ID_1_VERSION_1.asUri(), TAGS_EMPTY))
                .add(Arguments.of(PROPERTIES_MODEL, null, TAGS_ONE_KEY))
                .add(Arguments.of(PROPERTIES_MODEL, VERSIONED_KEY_ENTITY_ID_1_VERSION_1.asUri(), null))
                .build();
    }

    @ParameterizedTest
    @MethodSource("invalidInputProvider")
    void testConstructorShouldThrowExceptionWhenCalledWithNulls(
            final KeyPropertiesModel attributes, final URI uri, final Map<String, String> tags) {
        //given

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> new KeyVaultKeyItemModel(attributes, uri, tags));

        //then + exception
    }
}
