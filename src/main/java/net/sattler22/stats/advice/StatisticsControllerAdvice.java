package net.sattler22.stats.advice;

import jakarta.validation.ValidationException;
import jakarta.validation.constraints.NotNull;
import net.sattler22.stats.exception.ExpirationException;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Real-Time Statistics REST Controller Exception Aspect
 *
 * @author Pete Sattler
 * @since March 2022
 * @version May 2025
 */
@RestControllerAdvice
public final class StatisticsControllerAdvice extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ArithmeticException.class)
    public ResponseEntity<Object> handleArithmeticException(ArithmeticException exception, WebRequest webRequest) {
        return handleExceptionImpl(exception, HttpStatus.PRECONDITION_FAILED, webRequest);
    }

    @ExceptionHandler(ExpirationException.class)
    public ResponseEntity<Object> handleExpirationException(ExpirationException exception, WebRequest webRequest) {
        return handleExceptionImpl(exception, HttpStatus.CONFLICT, webRequest);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Object> handleValidationException(ValidationException exception, WebRequest webRequest) {
        return handleExceptionImpl(exception, HttpStatus.UNPROCESSABLE_ENTITY, webRequest);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGeneralException(Exception exception, WebRequest webRequest) {
        return handleExceptionImpl(exception, HttpStatus.INTERNAL_SERVER_ERROR, webRequest);
    }

    /**
     * Provide the API user with useful exception information (based on RFC 9457)
     */
    private ResponseEntity<Object> handleExceptionImpl(@NotNull Exception exception, @NotNull HttpStatusCode statusCode,
                                                       @NotNull WebRequest webRequest) {
        final ProblemDetail body = ProblemDetail.forStatusAndDetail(statusCode, exception.getMessage());
        body.setProperty("timestamp", System.currentTimeMillis());
        if (statusCode.is5xxServerError())
            logger.error(exception.getMessage(), exception);
        else
            logger.warn(exception.getMessage());
        return super.handleExceptionInternal(exception, body, HttpHeaders.EMPTY, statusCode, webRequest);
    }
}