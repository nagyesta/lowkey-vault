package com.github.nagyesta.lowkeyvault.filter;

import com.github.nagyesta.lowkeyvault.management.VaultImportExportExecutor;
import com.github.nagyesta.lowkeyvault.model.common.ErrorMessage;
import com.github.nagyesta.lowkeyvault.model.management.VaultBackupListModel;
import com.github.nagyesta.lowkeyvault.model.management.VaultBackupModel;
import com.github.nagyesta.lowkeyvault.service.vault.VaultService;
import jakarta.servlet.ServletException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.ObjectWriter;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpMethod.*;
import static org.springframework.http.HttpStatus.*;

class ContentExportFilterTest {

    private static final File EXPORT_FILE = new File("export.json");
    private static final String REQUEST_URI = "/path";
    private static final String AN_EXPECTED_MESSAGE = "An expected message";

    public static Stream<Arguments> emptyExportFileProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(new File("")))
                .add(Arguments.of(new File(" ")))
                .build();
    }

    public static Stream<Arguments> exportParameterProvider() {
        final var builder = Stream.<Arguments>builder();
        for (final var httpMethod : List.of(PUT, DELETE, PATCH, POST)) {
            for (final var httpStatus : List.of(ACCEPTED, OK, NO_CONTENT, SEE_OTHER, NOT_MODIFIED)) {
                builder.add(Arguments.of(httpMethod, httpStatus));
            }
        }
        return builder.build();
    }

    @ParameterizedTest
    @MethodSource("emptyExportFileProvider")
    @NullSource
    void testDoFilterInternalShouldNotExportTheContentWhenTheExportFileIsNotSet(final File exportfile)
            throws ServletException, IOException {
        //given
        final var executor = mock(VaultImportExportExecutor.class);
        final var vaultService = mock(VaultService.class);
        final var objectMapper = mock(ObjectMapper.class);
        final var underTest = new ContentExportFilter(executor, vaultService, objectMapper, exportfile);
        final var request = new MockHttpServletRequest(POST.name(), REQUEST_URI);
        final var response = new MockHttpServletResponse();
        final var filterChain = new MockFilterChain();

        //when
        underTest.doFilterInternal(request, response, filterChain);

        //then
        //no calls were made to perform the export
        verifyNoMoreInteractions(executor, vaultService, objectMapper);
    }

    @ParameterizedTest
    @ValueSource(strings = {"get", "head", "option"})
    void testDoFilterInternalShouldNotExportTheContentWhenTheRequestCannotChangeTheState(final String method)
            throws ServletException, IOException {
        //given
        final var executor = mock(VaultImportExportExecutor.class);
        final var vaultService = mock(VaultService.class);
        final var objectMapper = mock(ObjectMapper.class);
        final var underTest = new ContentExportFilter(executor, vaultService, objectMapper, EXPORT_FILE);
        final var request = new MockHttpServletRequest(method, REQUEST_URI);
        final var response = new MockHttpServletResponse();
        final var filterChain = new MockFilterChain();

        //when
        underTest.doFilterInternal(request, response, filterChain);

        //then
        //no calls were made to perform the export
        verifyNoMoreInteractions(executor, vaultService, objectMapper);
    }

    @ParameterizedTest
    @ValueSource(ints = {400, 401, 404, 500, 503})
    void testDoFilterInternalShouldNotExportTheContentWhenTheResponseIndicatesAnError(final int status)
            throws ServletException, IOException {
        //given
        final var executor = mock(VaultImportExportExecutor.class);
        final var vaultService = mock(VaultService.class);
        final var objectMapper = mock(ObjectMapper.class);
        final var underTest = new ContentExportFilter(executor, vaultService, objectMapper, EXPORT_FILE);
        final var request = new MockHttpServletRequest(POST.name(), REQUEST_URI);
        final var response = new MockHttpServletResponse();
        response.setStatus(status);
        final var filterChain = new MockFilterChain();

        //when
        underTest.doFilterInternal(request, response, filterChain);

        //then
        //no calls were made to perform the export
        verifyNoMoreInteractions(executor, vaultService, objectMapper);
    }

    @ParameterizedTest
    @MethodSource("exportParameterProvider")
    void testDoFilterInternalShouldOverrideResponseContentWhenTheExportFails(
            final HttpMethod method,
            final HttpStatus status) throws ServletException, IOException {
        //given
        final var executor = mock(VaultImportExportExecutor.class);
        final var vaultService = mock(VaultService.class);
        final var list = List.of(new VaultBackupModel());
        when(executor.backupVaultList(vaultService)).thenReturn(list);
        final var objectMapper = mock(ObjectMapper.class);
        final var objectWriter = mock(ObjectWriter.class);
        when(objectMapper.writerWithDefaultPrettyPrinter()).thenReturn(objectWriter);
        doThrow(new RuntimeException(AN_EXPECTED_MESSAGE)).when(objectWriter).writeValue(eq(EXPORT_FILE), any());
        final var expectedBackup = new VaultBackupListModel();
        expectedBackup.setVaults(list);
        final var underTest = new ContentExportFilter(executor, vaultService, objectMapper, EXPORT_FILE);
        final var request = new MockHttpServletRequest(method.name(), REQUEST_URI);
        final var response = new MockHttpServletResponse();
        response.setStatus(status.value());
        final var filterChain = new MockFilterChain();

        //when
        underTest.doFilterInternal(request, response, filterChain);

        //then
        verify(executor).backupVaultList(same(vaultService));
        verify(objectMapper).writerWithDefaultPrettyPrinter();
        verify(objectWriter).writeValue(EXPORT_FILE, expectedBackup);
        verify(objectMapper).writeValue(any(PrintWriter.class), any(ErrorMessage.class));
        verifyNoMoreInteractions(executor, vaultService, objectMapper);
        assertEquals(INTERNAL_SERVER_ERROR.value(), response.getStatus());
    }

    @ParameterizedTest
    @MethodSource("exportParameterProvider")
    void testDoFilterInternalShouldExportTheContentWhenTheExportFileIsSetAndTheRequestCanChangeTheStateAndTheResponseIsSuccessful(
            final HttpMethod method,
            final HttpStatus status) throws ServletException, IOException {
        //given
        final var executor = mock(VaultImportExportExecutor.class);
        final var vaultService = mock(VaultService.class);
        final var list = List.of(new VaultBackupModel());
        when(executor.backupVaultList(vaultService)).thenReturn(list);
        final var objectMapper = mock(ObjectMapper.class);
        final var objectWriter = mock(ObjectWriter.class);
        when(objectMapper.writerWithDefaultPrettyPrinter()).thenReturn(objectWriter);
        final var expectedBackup = new VaultBackupListModel();
        expectedBackup.setVaults(list);
        final var underTest = new ContentExportFilter(executor, vaultService, objectMapper, EXPORT_FILE);
        final var request = new MockHttpServletRequest(method.name(), REQUEST_URI);
        final var response = new MockHttpServletResponse();
        response.setStatus(status.value());
        final var filterChain = new MockFilterChain();

        //when
        underTest.doFilterInternal(request, response, filterChain);

        //then
        verify(executor).backupVaultList(same(vaultService));
        verify(objectMapper).writerWithDefaultPrettyPrinter();
        verify(objectWriter).writeValue(EXPORT_FILE, expectedBackup);
        verifyNoMoreInteractions(executor, vaultService, objectMapper);
    }
}
