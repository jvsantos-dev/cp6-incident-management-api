package br.com.fiap.incidentmanagement.repository;

import br.com.fiap.incidentmanagement.model.Incident;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

/**
 * Filtros dinâmicos da consulta de ocorrências (RF08): aluno, turma, categoria, tipo e período.
 * Cada filtro só é aplicado quando o parâmetro é informado.
 */
public final class IncidentSpecifications {

    private IncidentSpecifications() {
    }

    public static Specification<Incident> filter(Long studentId, Long schoolClassId, String category, String type,
                                                 LocalDate startDate, LocalDate endDate, boolean includeDeleted) {
        Specification<Incident> spec = (root, query, cb) -> cb.conjunction();
        if (!includeDeleted) {
            spec = spec.and((root, query, cb) -> cb.isFalse(root.get("deleted")));
        }
        if (studentId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("student").get("id"), studentId));
        }
        if (schoolClassId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("schoolClass").get("id"), schoolClassId));
        }
        if (category != null && !category.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(cb.lower(root.get("category").get("name")), category.toLowerCase()));
        }
        if (type != null && !type.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(cb.lower(root.get("type").get("name")), type.toLowerCase()));
        }
        if (startDate != null) {
            spec = spec.and((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("registerDate"), startDate.atStartOfDay()));
        }
        if (endDate != null) {
            spec = spec.and((root, query, cb) ->
                    cb.lessThan(root.get("registerDate"), endDate.plusDays(1).atStartOfDay()));
        }
        return spec;
    }
}
