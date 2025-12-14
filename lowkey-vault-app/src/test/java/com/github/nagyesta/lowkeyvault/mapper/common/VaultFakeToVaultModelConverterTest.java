package com.github.nagyesta.lowkeyvault.mapper.common;

import com.github.nagyesta.lowkeyvault.TestConstants;
import com.github.nagyesta.lowkeyvault.TestConstantsUri;
import com.github.nagyesta.lowkeyvault.model.v7_2.common.constants.RecoveryLevel;
import com.github.nagyesta.lowkeyvault.service.vault.VaultFake;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class VaultFakeToVaultModelConverterTest {

    private VaultFakeToVaultModelConverter underTest;

    @BeforeEach
    void setUp() {
        underTest = new VaultFakeToVaultModelConverterImpl();
    }

    @Test
    void testConvertShouldReturnNullWhenCalledWithNull() {
        //given

        //when
        final var actual = underTest.convert(null);

        //then
        Assertions.assertNull(actual);
    }

    @Test
    void testConvertShouldConvertFieldsWhenCalledWithNotNull() {
        //given
        final var input = mock(VaultFake.class);
        when(input.baseUri()).thenReturn(TestConstantsUri.HTTPS_LOCALHOST_8443);
        when(input.getCreatedOn()).thenReturn(TestConstants.TIME_10_MINUTES_AGO);
        when(input.getDeletedOn()).thenReturn(TestConstants.NOW);
        when(input.getRecoveryLevel()).thenReturn(RecoveryLevel.CUSTOMIZED_RECOVERABLE);
        when(input.getRecoverableDays()).thenReturn(RecoveryLevel.MIN_RECOVERABLE_DAYS_INCLUSIVE);

        //when
        final var actual = underTest.convert(input);

        //then
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(TestConstantsUri.HTTPS_LOCALHOST_8443, actual.getBaseUri());
        Assertions.assertEquals(TestConstants.TIME_10_MINUTES_AGO, actual.getCreatedOn());
        Assertions.assertEquals(TestConstants.NOW, actual.getDeletedOn());
        Assertions.assertEquals(RecoveryLevel.CUSTOMIZED_RECOVERABLE, actual.getRecoveryLevel());
        Assertions.assertEquals(RecoveryLevel.MIN_RECOVERABLE_DAYS_INCLUSIVE, actual.getRecoverableDays());

    }
}
