package net.sattler22.stats.advice;

import javax.validation.ConstraintViolationException;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Real-Time Statistics REST Controller Exception Aspect
 *
 * @author Pete Sattler
 * @version March 2022
 */
@ControllerAdvice
public final class GlobalControllerAdvice extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = { ArithmeticException.class })
    public ResponseEntity<Object> handleSpecificException(ArithmeticException exception, WebRequest webRequest) {
        return handleExceptionImpl(exception, HttpStatus.BAD_REQUEST, webRequest);
    }

    @ExceptionHandler(value = { ConstraintViolationException.class })
    public ResponseEntity<Object> handleSpecificException(ConstraintViolationException exception, WebRequest webRequest) {
        return handleExceptionImpl(exception, HttpStatus.BAD_REQUEST, webRequest);
    }

    @ExceptionHandler({ Exception.class })
    public ResponseEntity<Object> handleGeneralException(Exception exception, WebRequest webRequest) {
        return handleExceptionImpl(exception, HttpStatus.INTERNAL_SERVER_ERROR, webRequest);
    }

    /**
     * Provide the API user with useful exception information
     */
    private ResponseEntity<Object> handleExceptionImpl(Exception exception, HttpStatus httpStatus, WebRequest webRequest) {
        if (httpStatus == HttpStatus.INTERNAL_SERVER_ERROR)
            logger.error(exception.getMessage(), exception);
        else
            logger.warn(exception.getMessage());
        final var errorResponseBody = new ErrorResponseBody(httpStatus, webRequest, exception.getMessage());
        return handleExceptionInternal(exception, errorResponseBody.body(), new HttpHeaders(), httpStatus, webRequest);
    }
}