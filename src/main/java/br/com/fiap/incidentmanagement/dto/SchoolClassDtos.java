package br.com.fiap.incidentmanagement.dto;

import br.com.fiap.incidentmanagement.model.SchoolClass;
import br.com.fiap.incidentmanagement.model.Semester;
import br.com.fiap.incidentmanagement.model.Shift;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class SchoolClassDtos {

    private SchoolClassDtos() {
    }

    public record SchoolClassRequest(
            @NotBlank @Size(max = 100) String name,
            @NotNull Shift shift,
            @NotNull @Min(2000) @Max(2100) Integer year,
            @NotNull Semester semester) {
    }

    public record SchoolClassUpdateRequest(
            @NotNull Long id,
            @Size(max = 100) String name,
            Shift shift,
            @Min(2000) @Max(2100) Integer year,
            Semester semester) {
    }

    public record SchoolClassResponse(
            Long id,
            String name,
            Shift shift,
            Integer year,
            Semester semester,
            boolean canceled,
            int totalStudents) {

        public static SchoolClassResponse from(SchoolClass schoolClass) {
            return new SchoolClassResponse(schoolClass.getId(), schoolClass.getName(), schoolClass.getShift(),
                    schoolClass.getYear(), schoolClass.getSemester(), schoolClass.isCanceled(),
                    schoolClass.getStudents().size());
        }
    }
}
