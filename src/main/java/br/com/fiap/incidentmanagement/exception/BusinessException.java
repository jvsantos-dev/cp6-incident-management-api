package br.com.fiap.incidentmanagement.exception;

/**
 * Violação de regra de negócio (respondida com HTTP 400).
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
