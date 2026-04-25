package ru.practicum.error;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Обработка ошибок валидации (400 Bad Request)
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ErrorResponse handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();
        List<String> errorMessages = fieldErrors.stream()
                .map(fe -> String.format("Field: %s. Error: %s. Value: %s",
                        fe.getField(),
                        fe.getDefaultMessage(),
                        (fe.getRejectedValue() != null) ? fe.getRejectedValue().toString() : "null"))
                .collect(Collectors.toList());

        return new ErrorResponse(
                "BAD_REQUEST",
                "Incorrectly made request.",
                String.join("; ", errorMessages),
                LocalDateTime.now()
        );
    }

    /**
     * Обработка конфликтов дубликат email
     */
    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ErrorResponse handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        String rootCauseMessage = Objects.requireNonNull(ex.getRootCause()).getMessage();

        return new ErrorResponse(
                "CONFLICT",
                "Integrity constraint has been violated.",
                rootCauseMessage,
                LocalDateTime.now()
        );
    }

    /**
     * Общая обработка любых других исключений (500 Internal Server Error)
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ErrorResponse handleAllUncaughtException(Exception ex) {
        return new ErrorResponse(
                "INTERNAL_SERVER_ERROR",
                "Internal server error.",
                ex.getMessage(),
                LocalDateTime.now()
        );
    }
}

