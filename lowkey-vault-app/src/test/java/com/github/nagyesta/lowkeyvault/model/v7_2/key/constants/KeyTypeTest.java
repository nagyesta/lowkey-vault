package com.github.nagyesta.lowkeyvault.model.v7_2.key.constants;

import com.github.nagyesta.lowkeyvault.model.v7_2.key.request.JsonWebKeyImportRequest;
import com.github.nagyesta.lowkeyvault.service.key.*;
import com.github.nagyesta.lowkeyvault.service.key.id.VersionedKeyEntityId;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.*;

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

    public static Stream<Arguments> typeCheckProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(KeyType.OCT_HSM, true, false, false))
                .add(Arguments.of(KeyType.OCT, true, false, false))
                .add(Arguments.of(KeyType.EC_HSM, false, true, false))
                .add(Arguments.of(KeyType.EC, false, true, false))
                .add(Arguments.of(KeyType.RSA_HSM, false, false, true))
                .add(Arguments.of(KeyType.RSA, false, false, true))
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

    @ParameterizedTest
    @MethodSource("typeCheckProvider")
    void testIsOctEcRsaShouldReturnTrueOnlyWhenCalledOnTheRightInstance(
            final KeyType underTest, final boolean oct, final boolean ec, final boolean rsa) {
        //given

        //when
        final boolean actualOct = underTest.isOct();
        final boolean actualEc = underTest.isEc();
        final boolean actualRsa = underTest.isRsa();

        //then
        Assertions.assertEquals(oct, actualOct);
        Assertions.assertEquals(ec, actualEc);
        Assertions.assertEquals(rsa, actualRsa);
    }

    @Test
    void testImportOctShouldCallToCreateKeyEntityWhenCalled() {
        //given
        final KeyType underTest = KeyType.OCT_HSM;
        final KeyVaultFake keyVaultFake = mock(KeyVaultFake.class);
        final VersionedKeyEntityId versionedKeyEntityId = mock(VersionedKeyEntityId.class);
        final JsonWebKeyImportRequest request = mock(JsonWebKeyImportRequest.class);
        when(keyVaultFake.importOctKeyVersion(same(versionedKeyEntityId), same(request))).thenReturn(versionedKeyEntityId);

        //when
        final VersionedKeyEntityId actual = underTest.importKey(keyVaultFake, versionedKeyEntityId, request);

        //then
        Assertions.assertSame(versionedKeyEntityId, actual);
        verify(keyVaultFake).importOctKeyVersion(same(versionedKeyEntityId), same(request));
        verifyNoMoreInteractions(keyVaultFake);
    }

    @Test
    void testImportEcShouldCallToCreateKeyEntityWhenCalled() {
        //given
        final KeyType underTest = KeyType.EC_HSM;
        final KeyVaultFake keyVaultFake = mock(KeyVaultFake.class);
        final VersionedKeyEntityId versionedKeyEntityId = mock(VersionedKeyEntityId.class);
        final JsonWebKeyImportRequest request = mock(JsonWebKeyImportRequest.class);
        when(keyVaultFake.importEcKeyVersion(same(versionedKeyEntityId), same(request))).thenReturn(versionedKeyEntityId);

        //when
        final VersionedKeyEntityId actual = underTest.importKey(keyVaultFake, versionedKeyEntityId, request);

        //then
        Assertions.assertSame(versionedKeyEntityId, actual);
        verify(keyVaultFake).importEcKeyVersion(same(versionedKeyEntityId), same(request));
        verifyNoMoreInteractions(keyVaultFake);
    }

    @Test
    void testImportRsaShouldCallToCreateKeyEntityWhenCalled() {
        //given
        final KeyType underTest = KeyType.RSA_HSM;
        final KeyVaultFake keyVaultFake = mock(KeyVaultFake.class);
        final VersionedKeyEntityId versionedKeyEntityId = mock(VersionedKeyEntityId.class);
        final JsonWebKeyImportRequest request = mock(JsonWebKeyImportRequest.class);
        when(keyVaultFake.importRsaKeyVersion(same(versionedKeyEntityId), same(request))).thenReturn(versionedKeyEntityId);

        //when
        final VersionedKeyEntityId actual = underTest.importKey(keyVaultFake, versionedKeyEntityId, request);

        //then
        Assertions.assertSame(versionedKeyEntityId, actual);
        verify(keyVaultFake).importRsaKeyVersion(same(versionedKeyEntityId), same(request));
        verifyNoMoreInteractions(keyVaultFake);
    }
}
