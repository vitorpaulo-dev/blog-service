package dev.vitorpaulo.blog.config.exception.infrastructure;

import dev.vitorpaulo.blog.config.exception.InternalException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;

import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<BusinessErrorResponse> handleBusinessException(BusinessException exception) {
        return ResponseEntity.status(exception.getStatus())
                .body(new BusinessErrorResponse(exception.getCode(), LocalDateTime.now(), exception.getDetails()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BusinessErrorResponse> handleException(Exception exception) {
        log.error("Unexpected error", exception);

        return handleBusinessException(new InternalException());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BusinessErrorResponse> handleValidationException(MethodArgumentNotValidException exception) {
        log.error("Exception transformed by handler.", exception);

        return ResponseEntity.badRequest().body(
                new BusinessErrorResponse(
                        ExceptionCode.FIELD_VALIDATION,
                        LocalDateTime.now(),
                        exception.getBindingResult()
                                .getFieldErrors()
                                .stream()
                                .collect(Collectors.toMap(
                                        FieldError::getField,
                                        error -> StringUtils.defaultIfBlank(error.getDefaultMessage(), "")
                                ))
                )
        );
    }
}
