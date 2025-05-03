package com.github.nagyesta.lowkeyvault.filter;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.nagyesta.lowkeyvault.management.VaultImportExportExecutor;
import com.github.nagyesta.lowkeyvault.model.common.ErrorMessage;
import com.github.nagyesta.lowkeyvault.model.management.VaultBackupListModel;
import com.github.nagyesta.lowkeyvault.service.vault.VaultService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import static com.github.nagyesta.lowkeyvault.filter.ContentExportFilter.PRECEDENCE;

@Component
@Slf4j
@Order(PRECEDENCE)
public class ContentExportFilter
        extends OncePerRequestFilter {

    static final int PRECEDENCE = 200;

    private static final Set<HttpMethod> METHODS_CHANGING_STATE = Set.of(
            HttpMethod.DELETE, HttpMethod.PATCH, HttpMethod.POST, HttpMethod.PUT);

    private final VaultImportExportExecutor vaultImportExportExecutor;
    private final VaultService vaultService;
    private final ObjectMapper objectMapper;
    private final File exportFile;
    private final ReentrantLock lock;

    public ContentExportFilter(
            @NonNull final VaultImportExportExecutor vaultImportExportExecutor,
            @NonNull final VaultService vaultService,
            @NonNull final ObjectMapper objectMapper,
            @Value("${LOWKEY_EXPORT_LOCATION}") final File exportFile) {
        this.vaultImportExportExecutor = vaultImportExportExecutor;
        this.vaultService = vaultService;
        this.objectMapper = objectMapper;
        this.exportFile = exportFile;
        lock = new ReentrantLock();
    }

    @SuppressWarnings("NullableProblems")
    @Override
    protected void doFilterInternal(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final FilterChain filterChain)
            throws ServletException, IOException {
        final var responseToUse = wrapResponseIfNecessary(request, response);
        filterChain.doFilter(request, responseToUse);
        finalizeResponse(responseToUse);
    }

    private HttpServletResponse wrapResponseIfNecessary(
            final HttpServletRequest request,
            final HttpServletResponse response) {
        final HttpServletResponse responseToUse;
        if (isEnabled() && canChangeState(request)) {
            responseToUse = new ContentCachingResponseWrapper(response);
        } else {
            responseToUse = response;
        }
        return responseToUse;
    }



    private void finalizeResponse(final HttpServletResponse response) throws IOException {
        if (response instanceof final ContentCachingResponseWrapper wrapper) {
            if (wasSuccessful(response)) {
                try {
                    doExport();
                } catch (final Exception e) {
                    wrapper.reset();
                    final var errorMessage = ErrorMessage.fromException(new IllegalStateException("Failed to export vault content.", e));
                    objectMapper.writeValue(wrapper.getWriter(), errorMessage);
                    wrapper.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                }
            }
            wrapper.copyBodyToResponse();
        }
    }

    private void doExport() throws IOException {
        lock.lock();
        try {
            log.info("Exporting application state to file '{}'.", exportFile.getAbsolutePath());
            final var backupModels = vaultImportExportExecutor.backupVaultList(vaultService);
            final var vaultBackupListModel = new VaultBackupListModel();
            vaultBackupListModel.setVaults(backupModels);
            objectMapper.writer(new DefaultPrettyPrinter())
                    .writeValue(exportFile, vaultBackupListModel);
        } finally {
            lock.unlock();
        }
    }

    private static boolean canChangeState(final HttpServletRequest request) {
        return METHODS_CHANGING_STATE.contains(HttpMethod.valueOf(request.getMethod()));
    }

    private static boolean wasSuccessful(final HttpServletResponse response) {
        return response.getStatus() < HttpServletResponse.SC_BAD_REQUEST;
    }

    private boolean isEnabled() {
        return exportFile != null && !exportFile.getName().isBlank();
    }

}
