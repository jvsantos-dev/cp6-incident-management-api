package br.com.fiap.incidentmanagement.dto;

import br.com.fiap.incidentmanagement.model.Student;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public final class StudentDtos {

    private StudentDtos() {
    }

    public record StudentRequest(
            @NotBlank @Size(max = 100) String name,
            @NotNull @Past @JsonFormat(pattern = "dd/MM/yyyy") LocalDate birthDate) {
    }

    public record StudentUpdateRequest(
            @NotNull Long id,
            @Size(max = 100) String name,
            @Past @JsonFormat(pattern = "dd/MM/yyyy") LocalDate birthDate) {
    }

    public record StudentResponse(
            Long id,
            String name,
            @JsonFormat(pattern = "dd/MM/yyyy") LocalDate birthDate,
            String imageUrl,
            boolean active) {

        public static StudentResponse from(Student student) {
            String imageUrl = student.hasImage() ? "/students/" + student.getId() + "/image" : null;
            return new StudentResponse(student.getId(), student.getName(), student.getBirthDate(),
                    imageUrl, student.isActive());
        }
    }
}
