package br.com.fiap.incidentmanagement.dto;

import br.com.fiap.incidentmanagement.model.Incident;
import br.com.fiap.incidentmanagement.model.IncidentStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

public final class IncidentDtos {

    public static final String DATE_TIME_PATTERN = "dd/MM/yyyy - HH:mm";

    private IncidentDtos() {
    }

    /**
     * Registro de ocorrência. Categoria e tipo são informados pelo nome
     * (valores devolvidos por GET /incidents/categories e GET /incidents/types/{categoria}).
     * Se a data não for informada, é usada a data/hora atual.
     */
    public record IncidentRequest(
            @NotNull Long idSchoolClass,
            @NotNull Long idStudent,
            @JsonFormat(pattern = DATE_TIME_PATTERN) LocalDateTime registerDate,
            @NotBlank String category,
            @NotBlank String type,
            @NotBlank @Size(max = 2000) String description) {
    }

    public record IncidentUpdateRequest(
            @NotNull Long id,
            Long idSchoolClass,
            Long idStudent,
            @JsonFormat(pattern = DATE_TIME_PATTERN) LocalDateTime registerDate,
            String category,
            String type,
            @Size(max = 2000) String description) {
    }

    public record StatusUpdateRequest(
            @NotNull Long id,
            @NotNull IncidentStatus status,
            @JsonFormat(pattern = DATE_TIME_PATTERN) LocalDateTime updateDate) {
    }

    public record StatusResponse(String name, String description) {

        public static StatusResponse from(IncidentStatus status) {
            return new StatusResponse(status.name(), status.getDescription());
        }
    }

    public record IncidentResponse(
            Long id,
            Long idSchoolClass,
            String schoolClassName,
            Long idStudent,
            String studentName,
            Long idUser,
            String userName,
            @JsonFormat(pattern = DATE_TIME_PATTERN) LocalDateTime registerDate,
            String category,
            String type,
            String description,
            IncidentStatus status,
            @JsonFormat(pattern = DATE_TIME_PATTERN) LocalDateTime updateDate,
            boolean deleted) {

        public static IncidentResponse from(Incident incident) {
            return new IncidentResponse(
                    incident.getId(),
                    incident.getSchoolClass().getId(),
                    incident.getSchoolClass().getName(),
                    incident.getStudent().getId(),
                    incident.getStudent().getName(),
                    incident.getUser().getId(),
                    incident.getUser().getName(),
                    incident.getRegisterDate(),
                    incident.getCategory().getName(),
                    incident.getType().getName(),
                    incident.getDescription(),
                    incident.getStatus(),
                    incident.getUpdateDate(),
                    incident.isDeleted());
        }
    }

    /**
     * Dados do dashboard do coordenador. O nome "bySchooclass" é mantido por compatibilidade com o frontend.
     */
    public record IncidentSummary(
            long active,
            long waiting,
            long progressing,
            long solved,
            long unsolved,
            long closed,
            List<KeyValue> byCategory,
            List<KeyValue> byType,
            List<KeyValue> bySchooclass,
            List<KeyValue> byStudent,
            List<KeyValue> byPeriod) {
    }
}
