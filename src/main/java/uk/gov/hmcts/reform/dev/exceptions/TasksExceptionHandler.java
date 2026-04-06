package uk.gov.hmcts.reform.dev.exceptions;

import java.time.LocalDateTime;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import uk.gov.hmcts.reform.dev.dto.responses.ErrorResponse;

/**
 * Exception handler for tasks.
 * Could be reused across different implementations.
 */
@Slf4j
@ControllerAdvice
public class TasksExceptionHandler {

    @ExceptionHandler(TaskNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleTaskNotFoundException(
        TaskNotFoundException ex,
        HttpServletRequest request
    ) {
        ErrorResponse errorResponse = ErrorResponse.builder()
            .errorCode("error.tasks.not.found")
            .message(ex.getMessage())
            .status(HttpStatus.NOT_FOUND.value())
            .path(request.getRequestURI())
            .timestamp(LocalDateTime.now())
            .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(InvalidStateTransitionException.class)
    public ResponseEntity<ErrorResponse> handleInvalidStateTransitionException(
        InvalidStateTransitionException ex,
        HttpServletRequest request
    ) {
        ErrorResponse errorResponse = ErrorResponse.builder()
            .errorCode("error.tasks.status.invalid.transition")
            .message(ex.getMessage())
            .status(HttpStatus.BAD_REQUEST.value())
            .path(request.getRequestURI())
            .timestamp(LocalDateTime.now())
            .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
        MethodArgumentNotValidException ex,
        HttpServletRequest request
    ) {

        ErrorResponse errorResponse = ErrorResponse.builder()
            .errorCode("error.tasks.validation.failed")
            .message("An error occurred due to validation schema mismatch")
            .status(HttpStatus.BAD_REQUEST.value())
            .path(request.getRequestURI())
            .timestamp(LocalDateTime.now())
            .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
        Exception ex,
        HttpServletRequest request
    ) {
        ErrorResponse errorResponse = ErrorResponse.builder()
            .errorCode("error.internal.server.error")
            .message("An unexpected error occurred: " + ex.getMessage())
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .path(request.getRequestURI())
            .timestamp(LocalDateTime.now())
            .build();

        log.error("Unexpected error: ", ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

}
