package com.github.nagyesta.lowkeyvault.service.vault.impl;

import com.github.nagyesta.lowkeyvault.service.vault.VaultFake;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Stream;

import static com.github.nagyesta.lowkeyvault.TestConstantsUri.*;

class VaultServiceImplTest {

    public static final int WAIT_MILLIS = 2000;
    private static final int THREADS = 3;

    public static Stream<Arguments> valueProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(Collections.emptyList(), null, false))
                .add(Arguments.of(Collections.emptyList(), HTTPS_LOCALHOST, false))
                .add(Arguments.of(Collections.singletonList(HTTPS_LOCALHOST_80), HTTPS_LOCALHOST, false))
                .add(Arguments.of(Collections.singletonList(HTTPS_LOCALHOST_8443), HTTPS_LOCALHOST, false))
                .add(Arguments.of(Collections.singletonList(HTTPS_LOCALHOST_8443), HTTPS_LOCALHOST_8443, true))
                .add(Arguments.of(List.of(HTTPS_LOCALHOST, HTTPS_LOCALHOST_80, HTTPS_LOCALHOST_8443), HTTPS_LOCALHOST_8443, true))
                .build();
    }

    @ParameterizedTest
    @MethodSource("valueProvider")
    void testFindByUriShouldUseFullMatchWhenCalled(final List<URI> vaults, final URI lookup, final boolean expected) {
        //given
        final VaultServiceImpl underTest = new VaultServiceImpl();
        vaults.forEach(underTest::create);

        //when
        final VaultFake actual = underTest.findByUri(lookup);

        //then
        if (expected) {
            Assertions.assertNotNull(actual);
            Assertions.assertEquals(lookup, actual.baseUri());
        } else {
            Assertions.assertNull(actual);
        }
    }

    @Test
    void testCreateShouldBeSynchronized() {
        //given
        final VaultServiceImpl underTest = new VaultServiceImpl() {
            @Override
            public VaultFake findByUri(final URI uri) {
                //Make sure exists checks are slow to generate race conditions
                Assertions.assertDoesNotThrow(() -> Thread.sleep(WAIT_MILLIS));
                return super.findByUri(uri);
            }
        };

        //when
        final List<Future<VaultFake>> futures = new ArrayList<>();
        final List<VaultFake> fakes = new ArrayList<>();
        ExecutorService executorService = null;
        try {
            //start more create calls in parallel
            executorService = Executors.newFixedThreadPool(THREADS);
            for (int i = 0; i < THREADS; i++) {
                futures.add(executorService.submit(() -> underTest.create(HTTPS_LOCALHOST)));
            }
            for (final Future<VaultFake> future : futures) {
                Assertions.assertDoesNotThrow(() -> fakes.add(future.get()));
            }
        } finally {
            Optional.ofNullable(executorService).ifPresent(ExecutorService::shutdownNow);
        }

        //then
        final VaultFake firstFake = fakes.get(0);
        fakes.forEach(fake -> Assertions.assertSame(firstFake, fake));
    }

}
