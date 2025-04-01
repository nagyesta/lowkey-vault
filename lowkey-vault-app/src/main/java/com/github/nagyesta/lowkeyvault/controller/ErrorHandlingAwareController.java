package com.github.nagyesta.lowkeyvault.controller;

import com.github.nagyesta.lowkeyvault.model.common.ErrorModel;
import com.github.nagyesta.lowkeyvault.service.exception.AlreadyExistsException;
import com.github.nagyesta.lowkeyvault.service.exception.CryptoException;
import com.github.nagyesta.lowkeyvault.service.exception.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@Slf4j
public class ErrorHandlingAwareController {

    @ExceptionHandler({
            IllegalStateException.class,
            AlreadyExistsException.class,
            CryptoException.class,
            NotFoundException.class})
    public ResponseEntity<ErrorModel> handleException(final Exception exception) {
        var status = HttpStatus.INTERNAL_SERVER_ERROR;
        final var exceptionClass = exception.getClass();
        if (exceptionClass.isAnnotationPresent(ResponseStatus.class)) {
            status = exceptionClass.getAnnotation(ResponseStatus.class).value();
        }
        log.error("Returning error model due to exception.", exception);
        return ResponseEntity.status(status).body(ErrorModel.fromException(exception));
    }

    @ExceptionHandler({IllegalArgumentException.class})
    public ResponseEntity<ErrorModel> handleArgumentException(final Exception exception) {
        log.error("Returning error model due to exception.", exception);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorModel.fromException(exception));
    }
}
