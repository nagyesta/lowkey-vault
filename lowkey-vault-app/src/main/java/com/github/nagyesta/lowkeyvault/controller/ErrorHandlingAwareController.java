package com.github.nagyesta.lowkeyvault.controller;

import com.github.nagyesta.lowkeyvault.model.common.ErrorModel;
import com.github.nagyesta.lowkeyvault.service.exception.AlreadyExistsException;
import com.github.nagyesta.lowkeyvault.service.exception.CryptoException;
import com.github.nagyesta.lowkeyvault.service.exception.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

public class ErrorHandlingAwareController {

    @ExceptionHandler({IllegalStateException.class, AlreadyExistsException.class, CryptoException.class, NotFoundException.class})
    public ResponseEntity<ErrorModel> handleException(final Exception exception) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        final Class<? extends Exception> exceptionClass = exception.getClass();
        if (exceptionClass.isAnnotationPresent(ResponseStatus.class)) {
            status = exceptionClass.getAnnotation(ResponseStatus.class).value();
        }
        return ResponseEntity.status(status).body(ErrorModel.fromException(exception));
    }
}
