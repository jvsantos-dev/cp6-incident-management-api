package br.com.fiap.incidentmanagement.exception;

/**
 * Recurso não encontrado (respondido com HTTP 404).
 */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }
}
