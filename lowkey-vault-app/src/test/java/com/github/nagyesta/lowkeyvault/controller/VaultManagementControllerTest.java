package com.github.nagyesta.lowkeyvault.controller;

import com.github.nagyesta.lowkeyvault.TestConstants;
import com.github.nagyesta.lowkeyvault.mapper.common.VaultFakeToVaultModelConverter;
import com.github.nagyesta.lowkeyvault.model.management.VaultModel;
import com.github.nagyesta.lowkeyvault.model.v7_2.common.constants.RecoveryLevel;
import com.github.nagyesta.lowkeyvault.service.vault.VaultFake;
import com.github.nagyesta.lowkeyvault.service.vault.VaultService;
import com.github.nagyesta.lowkeyvault.service.vault.impl.VaultFakeImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

import java.net.URI;
import java.util.Collections;
import java.util.stream.Stream;

import static com.github.nagyesta.lowkeyvault.TestConstants.NUMBER_OF_SECONDS_IN_10_MINUTES;
import static com.github.nagyesta.lowkeyvault.TestConstantsUri.*;
import static org.mockito.Mockito.*;

class VaultManagementControllerTest {

    private static final VaultModel VAULT_MODEL = createVaultModel(HTTPS_DEFAULT_LOWKEY_VAULT, false);
    private static final VaultModel VAULT_MODEL_DELETED = createVaultModel(HTTPS_DEFAULT_LOWKEY_VAULT_8443, true);

    public static Stream<Arguments> invalidProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(null, null))
                .add(Arguments.of(mock(VaultService.class), null))
                .add(Arguments.of(null, mock(VaultFakeToVaultModelConverter.class)))
                .build();
    }

    private static VaultModel createVaultModel(final URI uri, final boolean deleted) {
        final var model = new VaultModel();
        model.setBaseUri(uri);
        model.setRecoveryLevel(RecoveryLevel.CUSTOMIZED_RECOVERABLE);
        model.setRecoverableDays(RecoveryLevel.MIN_RECOVERABLE_DAYS_INCLUSIVE);
        model.setCreatedOn(TestConstants.TIME_10_MINUTES_AGO);
        if (deleted) {
            model.setDeletedOn(TestConstants.NOW);
        }
        return model;
    }

    @ParameterizedTest
    @MethodSource("invalidProvider")
    void testConstructorThrowsExceptionWhenCalledWithNull(
            final VaultService vaultService,
            final VaultFakeToVaultModelConverter vaultFakeToVaultModelConverter) {

        //given

        //when
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new VaultManagementController(vaultService, vaultFakeToVaultModelConverter));

        //then + exception
    }

    @Nested
    class FunctionalTest {
        @Mock
        private VaultService vaultService;
        @Mock
        private VaultFakeToVaultModelConverter converter;
        @InjectMocks
        private VaultManagementController underTest;
        private AutoCloseable openMocks;
        private VaultFake vaultFakeActive;
        private VaultFake vaultFakeDeleted;


        @BeforeEach
        void setUp() {
            openMocks = MockitoAnnotations.openMocks(this);
            vaultFakeActive = new VaultFakeImpl(HTTPS_DEFAULT_LOWKEY_VAULT);
            vaultFakeDeleted = new VaultFakeImpl(HTTPS_DEFAULT_LOWKEY_VAULT_8443);
            when(vaultService.create(
                    eq(HTTPS_DEFAULT_LOWKEY_VAULT),
                    eq(RecoveryLevel.CUSTOMIZED_RECOVERABLE),
                    eq(RecoveryLevel.MIN_RECOVERABLE_DAYS_INCLUSIVE),
                    isNull())).thenReturn(vaultFakeActive);
            when(vaultService.findByUri(HTTPS_DEFAULT_LOWKEY_VAULT)).thenReturn(vaultFakeActive);
            when(vaultService.findByUri(HTTPS_DEFAULT_LOWKEY_VAULT_8443)).thenReturn(vaultFakeDeleted);
            when(vaultService.list()).thenReturn(Collections.singletonList(vaultFakeActive));
            when(vaultService.listDeleted()).thenReturn(Collections.singletonList(vaultFakeDeleted));
            when(vaultService.delete(vaultFakeActive.baseUri())).thenReturn(true);
            when(converter.convert(same(vaultFakeActive))).thenReturn(VAULT_MODEL);
            when(converter.convert(same(vaultFakeDeleted))).thenReturn(VAULT_MODEL_DELETED);
            when(converter.convertNonNull(same(vaultFakeActive))).thenReturn(VAULT_MODEL);
            when(converter.convertNonNull(same(vaultFakeDeleted))).thenReturn(VAULT_MODEL_DELETED);
        }

        @AfterEach
        void tearDown() throws Exception {
            openMocks.close();
        }

        @Test
        void testCreateVaultShouldUseInputParametersWhenCalledWithValidInput() {
            //given

            //when
            final var actual = underTest.createVault(VAULT_MODEL);

            //then
            Assertions.assertEquals(VAULT_MODEL, actual.getBody());
            Assertions.assertEquals(HttpStatus.OK, actual.getStatusCode());
            final var inOrder = inOrder(vaultService, converter);
            inOrder.verify(vaultService)
                    .create(eq(HTTPS_DEFAULT_LOWKEY_VAULT),
                            eq(VAULT_MODEL.getRecoveryLevel()),
                            eq(VAULT_MODEL.getRecoverableDays()),
                            isNull());
            inOrder.verify(converter).convert(vaultFakeActive);
            verifyNoMoreInteractions(vaultService, converter);
        }

        @Test
        void testListVaultsShouldConvertVaultsWhenCalled() {
            //given

            //when
            final var actual = underTest.listVaults();

            //then
            Assertions.assertEquals(Collections.singletonList(VAULT_MODEL), actual.getBody());
            Assertions.assertEquals(HttpStatus.OK, actual.getStatusCode());
            final var inOrder = inOrder(vaultService, converter);
            inOrder.verify(vaultService).list();
            inOrder.verify(converter).convertNonNull(vaultFakeActive);
            verifyNoMoreInteractions(vaultService, converter);
        }

        @Test
        void testListDeletedVaultsShouldConvertVaultsWhenCalled() {
            //given

            //when
            final var actual = underTest.listDeletedVaults();

            //then
            Assertions.assertEquals(Collections.singletonList(VAULT_MODEL_DELETED), actual.getBody());
            Assertions.assertEquals(HttpStatus.OK, actual.getStatusCode());
            final var inOrder = inOrder(vaultService, converter);
            inOrder.verify(vaultService).listDeleted();
            inOrder.verify(converter).convertNonNull(vaultFakeDeleted);
            verifyNoMoreInteractions(vaultService, converter);
        }

        @Test
        void testDeleteVaultShouldCallServiceWhenCalled() {
            //given

            //when
            final var actual = underTest.deleteVault(HTTPS_DEFAULT_LOWKEY_VAULT);

            //then
            Assertions.assertEquals(true, actual.getBody());
            Assertions.assertEquals(HttpStatus.OK, actual.getStatusCode());
            verify(vaultService).delete(HTTPS_DEFAULT_LOWKEY_VAULT);
            verifyNoMoreInteractions(vaultService, converter);
        }

        @Test
        void testRecoverVaultShouldCallServiceWhenCalled() {
            //given

            //when
            final var actual = underTest.recoverVault(HTTPS_DEFAULT_LOWKEY_VAULT_8443);

            //then
            Assertions.assertEquals(VAULT_MODEL_DELETED, actual.getBody());
            Assertions.assertEquals(HttpStatus.OK, actual.getStatusCode());
            final var inOrder = inOrder(vaultService, converter);
            inOrder.verify(vaultService).recover(HTTPS_DEFAULT_LOWKEY_VAULT_8443);
            inOrder.verify(vaultService).findByUri(HTTPS_DEFAULT_LOWKEY_VAULT_8443);
            inOrder.verify(converter).convert(same(vaultFakeDeleted));
            verifyNoMoreInteractions(vaultService, converter);
        }

        @Test
        void testPurgeVaultShouldCallServiceWhenCalled() {
            //given
            when(vaultService.purge(HTTPS_DEFAULT_LOWKEY_VAULT_8443)).thenReturn(true);

            //when
            final var actual = underTest.purgeVault(HTTPS_DEFAULT_LOWKEY_VAULT_8443);

            //then
            Assertions.assertEquals(true, actual.getBody());
            Assertions.assertEquals(HttpStatus.OK, actual.getStatusCode());
            verify(vaultService).purge(HTTPS_DEFAULT_LOWKEY_VAULT_8443);
            verifyNoMoreInteractions(vaultService, converter);
        }

        @Test
        void testAliasUpdateShouldCallServiceWhenCalled() {
            //given
            when(vaultService.updateAlias(HTTPS_DEFAULT_LOWKEY_VAULT_8443, HTTPS_LOCALHOST_80, HTTPS_LOOP_BACK_IP))
                    .thenReturn(vaultFakeActive);
            when(converter.convert(vaultFakeActive)).thenReturn(VAULT_MODEL);

            //when
            final var actual = underTest
                    .aliasUpdate(HTTPS_DEFAULT_LOWKEY_VAULT_8443, HTTPS_LOCALHOST_80, HTTPS_LOOP_BACK_IP);

            //then
            Assertions.assertSame(VAULT_MODEL, actual.getBody());
            Assertions.assertEquals(HttpStatus.OK, actual.getStatusCode());
            verify(vaultService).updateAlias(HTTPS_DEFAULT_LOWKEY_VAULT_8443, HTTPS_LOCALHOST_80, HTTPS_LOOP_BACK_IP);
            verify(converter).convert(vaultFakeActive);
            verifyNoMoreInteractions(vaultService, converter);
        }

        @Test
        void testTimeShiftGlobalShouldCallServiceWhenCalled() {
            //given

            //when
            final var actual = underTest.timeShiftAll(NUMBER_OF_SECONDS_IN_10_MINUTES, false);

            //then
            Assertions.assertEquals(HttpStatus.NO_CONTENT, actual.getStatusCode());
            verify(vaultService).timeShift(NUMBER_OF_SECONDS_IN_10_MINUTES, false);
            verifyNoMoreInteractions(vaultService);
        }

        @Test
        void testTimeShiftSingleShouldCallServiceWhenCalled() {
            //given
            final var createdOn = vaultFakeActive.getCreatedOn();
            when(vaultService.findByUriIncludeDeleted(HTTPS_DEFAULT_LOWKEY_VAULT)).thenReturn(vaultFakeActive);

            //when
            final var actual = underTest
                    .timeShiftSingle(HTTPS_DEFAULT_LOWKEY_VAULT, NUMBER_OF_SECONDS_IN_10_MINUTES, true);

            //then
            Assertions.assertEquals(HttpStatus.NO_CONTENT, actual.getStatusCode());
            Assertions.assertEquals(createdOn.minusSeconds(NUMBER_OF_SECONDS_IN_10_MINUTES), vaultFakeActive.getCreatedOn());
            verify(vaultService).findByUriIncludeDeleted(HTTPS_DEFAULT_LOWKEY_VAULT);
            verifyNoMoreInteractions(vaultService);
        }
    }
}
