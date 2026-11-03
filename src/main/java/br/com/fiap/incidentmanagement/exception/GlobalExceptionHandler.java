package br.com.fiap.incidentmanagement.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    public record ErrorResponse(LocalDateTime timestamp, int status, String error, List<String> messages) {

        static ResponseEntity<ErrorResponse> of(HttpStatus status, List<String> messages) {
            return ResponseEntity.status(status)
                    .body(new ErrorResponse(LocalDateTime.now(), status.value(), status.getReasonPhrase(), messages));
        }

        static ResponseEntity<ErrorResponse> of(HttpStatus status, String message) {
            return of(status, List.of(message));
        }
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException ex) {
        return ErrorResponse.of(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler({BusinessException.class, IllegalArgumentException.class})
    public ResponseEntity<ErrorResponse> handleBusiness(RuntimeException ex) {
        return ErrorResponse.of(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        List<String> messages = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();
        return ErrorResponse.of(HttpStatus.BAD_REQUEST, messages);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ErrorResponse> handleUnreadable(Exception ex) {
        return ErrorResponse.of(HttpStatus.BAD_REQUEST,
                "Requisição inválida: verifique os campos, datas (dd/MM/yyyy ou dd/MM/yyyy - HH:mm) e valores enumerados.");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleIntegrity(DataIntegrityViolationException ex) {
        return ErrorResponse.of(HttpStatus.CONFLICT, "Operação viola a integridade dos dados (registro duplicado ou em uso).");
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleUploadSize(MaxUploadSizeExceededException ex) {
        return ErrorResponse.of(HttpStatus.PAYLOAD_TOO_LARGE, "Arquivo excede o tamanho máximo de 2MB.");
    }

    @ExceptionHandler({BadCredentialsException.class, DisabledException.class})
    public ResponseEntity<ErrorResponse> handleBadCredentials(Exception ex) {
        return ErrorResponse.of(HttpStatus.UNAUTHORIZED, "Login ou senha inválidos.");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        return ErrorResponse.of(HttpStatus.FORBIDDEN, ex.getMessage());
    }
}
