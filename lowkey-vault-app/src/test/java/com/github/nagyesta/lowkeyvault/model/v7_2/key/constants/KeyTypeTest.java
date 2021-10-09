package com.github.nagyesta.lowkeyvault.model.v7_2.key.constants;

import com.github.nagyesta.lowkeyvault.service.key.ReadOnlyAesKeyVaultKeyEntity;
import com.github.nagyesta.lowkeyvault.service.key.ReadOnlyEcKeyVaultKeyEntity;
import com.github.nagyesta.lowkeyvault.service.key.ReadOnlyKeyVaultKeyEntity;
import com.github.nagyesta.lowkeyvault.service.key.ReadOnlyRsaKeyVaultKeyEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

class KeyTypeTest {

    public static Stream<Arguments> valueProvider() {
        final List<KeyType> list = new ArrayList<>();
        list.add(null);
        list.addAll(Arrays.asList(KeyType.values()));
        return list.stream()
                .map(value -> Arguments.of(Optional.ofNullable(value).map(KeyType::getValue).orElse(null), value));
    }

    public static Stream<Arguments> entityProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(KeyType.RSA, ReadOnlyRsaKeyVaultKeyEntity.class))
                .add(Arguments.of(KeyType.RSA_HSM, ReadOnlyRsaKeyVaultKeyEntity.class))
                .add(Arguments.of(KeyType.EC, ReadOnlyEcKeyVaultKeyEntity.class))
                .add(Arguments.of(KeyType.EC_HSM, ReadOnlyEcKeyVaultKeyEntity.class))
                .add(Arguments.of(KeyType.OCT_HSM, ReadOnlyAesKeyVaultKeyEntity.class))
                .build();
    }

    public static Stream<Arguments> validationProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(KeyType.RSA, -1))
                .add(Arguments.of(KeyType.RSA_HSM, -1))
                .add(Arguments.of(KeyType.OCT, -1))
                .add(Arguments.of(KeyType.OCT_HSM, -1))
                .build();
    }

    @ParameterizedTest
    @MethodSource("valueProvider")
    void testForValueShouldReturnEnumWhenValueStringMatches(final String input, final KeyType expected) {
        //given

        //when
        final KeyType actual = KeyType.forValue(input);

        //then
        Assertions.assertEquals(expected, actual);
    }


    @ParameterizedTest
    @MethodSource("entityProvider")
    void testEntityClassShouldReturnClassWhenCalled(final KeyType underTest, final Class<?> expected) {
        //given

        //when
        final Class<? extends ReadOnlyKeyVaultKeyEntity> actual = underTest.entityClass();

        //then
        Assertions.assertEquals(expected, actual);
    }

    @ParameterizedTest
    @MethodSource("validationProvider")
    void testValidateShouldThrowExceptionWhenCalledWithInvalidValue(final KeyType underTest, final Integer value) {
        //given

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.validate(value, Integer.class));

        //then + exception
    }
}
