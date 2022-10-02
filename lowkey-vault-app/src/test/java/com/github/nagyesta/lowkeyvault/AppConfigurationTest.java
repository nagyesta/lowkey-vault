package com.github.nagyesta.lowkeyvault;

import com.github.nagyesta.lowkeyvault.service.vault.VaultService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class AppConfigurationTest {

    private static final int PORT = 8443;

    @Test
    void testDoAddVaultAliasesShouldSplitAndApplyAliasPairsWhenCalledWithValidInput() {
        //given
        final AppConfiguration underTest = new AppConfiguration();
        underTest.setPort(PORT);
        underTest.setAliases("primary.localhost=localhost:30443,secondary.localhost=localhost:<port>");
        final VaultService service = mock(VaultService.class);

        //when
        underTest.doAddVaultAliases(service);

        //then
        verify(service).updateAlias(eq(URI.create("https://primary.localhost:8443")), eq(URI.create("https://localhost:30443")), isNull());
        verify(service).updateAlias(eq(URI.create("https://secondary.localhost:8443")), eq(URI.create("https://localhost:8443")), isNull());
    }

    @Test
    void testDoAddVaultAliasesShouldThrowExceptionWhenCalledWithInvalidPairs() {
        //given
        final AppConfiguration underTest = new AppConfiguration();
        underTest.setPort(PORT);
        underTest.setAliases("primary.localhost,secondary.localhost=localhost:<port>=localhost:30443");
        final VaultService service = mock(VaultService.class);

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.doAddVaultAliases(service));

        //then + exception
    }
}
