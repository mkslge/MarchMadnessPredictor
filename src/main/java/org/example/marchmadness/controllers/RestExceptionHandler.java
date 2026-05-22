package org.example.marchmadness.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RestExceptionHandler {

    /*
     * Command: Convert database configuration errors into clear API responses.
     * Preconditions: A controller request failed because statistics persistence is unavailable.
     * Postconditions: Returns a 503 response with an actionable message for the frontend.
     *
     * Keeps expected local-development configuration failures from becoming anonymous 500 errors.
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalStateException(IllegalStateException exception) {
        if (isPostgreSqlConfigurationError(exception)) {
            return ResponseEntity
                    .status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(new ApiErrorResponse(exception.getMessage()));
        }

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiErrorResponse("The server could not complete the request."));
    }

    /*
     * Command: Identify errors caused by missing PostgreSQL persistence configuration.
     * Preconditions: The service threw an IllegalStateException.
     * Postconditions: Returns true only for the repository's PostgreSQL configuration failures.
     *
     * Avoids exposing unrelated internal exception messages to API callers.
     */
    private boolean isPostgreSqlConfigurationError(IllegalStateException exception) {
        String message = exception.getMessage();
        return message != null
                && (message.startsWith("PostgreSQL is not configured")
                || message.startsWith("PostgreSQL transactions are not configured"));
    }

    private record ApiErrorResponse(String message) {
    }
}
