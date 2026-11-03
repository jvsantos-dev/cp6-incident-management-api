package br.com.fiap.incidentmanagement.dto;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Representação estável de uma página de resultados (formato consumido pelo frontend).
 */
public record PageResponse<T>(
        List<T> content,
        int number,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last) {

    public static <T> PageResponse<T> of(Page<T> page) {
        return new PageResponse<>(page.getContent(), page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages(), page.isFirst(), page.isLast());
    }
}
