package net.sattler22.stats.advice;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import net.jcip.annotations.Immutable;

/**
 * Real-Time Statistics REST Controller Error Body Response
 *
 * @author Pete Sattler
 * @version March 2022
 */
@Immutable
final class ErrorResponseBody {

    private final String errorId;
    private final HttpStatus httpStatus;
    private final Map<String, Object> body = new LinkedHashMap<>();

    /**
     * Constructs a new standard error response
     *
     * @param httpStatus The HTTP error status
     * @param webRequest The web request
     * @param errorMessage The error message
     */
    ErrorResponseBody(HttpStatus httpStatus, WebRequest webRequest, String errorMessage) {
        this.httpStatus = Objects.requireNonNull(httpStatus, "HTTP status is required");
        Objects.requireNonNull(webRequest, "Web request is required");
        if (errorMessage == null || errorMessage.isBlank())
            throw new IllegalArgumentException("Error message is required");
        this.errorId = UUID.randomUUID().toString();
        this.body.put("errorId", errorId);
        final var httpServletRequest = ((ServletWebRequest) webRequest).getRequest();
        this.body.put("uri", httpServletRequest.getRequestURI());
        if (httpServletRequest.getPathInfo() != null)
            this.body.put("path", httpServletRequest.getPathInfo());
        if (httpServletRequest.getQueryString() != null)
            this.body.put("queryString", httpServletRequest.getQueryString());
        this.body.put("statusCode", this.httpStatus.value());
        this.body.put("errorMessage", errorMessage);
        this.body.put("timestamp", OffsetDateTime.now());
    }

    String errorId() {
        return errorId;
    }

    HttpStatus httpStatus() {
        return httpStatus;
    }

    Map<String, Object> body() {
        return Map.copyOf(body);
    }

    @Override
    public int hashCode() {
        return Objects.hash(errorId);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (other == null)
            return false;
        if (getClass() != other.getClass())
            return false;
        final var that = (ErrorResponseBody) other;
        return Objects.equals(errorId, that.errorId);
    }

    @Override
    public String toString() {
        return String.format("%s [errorId=%s, httpStatus=%s, body=%s]", getClass().getSimpleName(), errorId, httpStatus, body);
    }
}
