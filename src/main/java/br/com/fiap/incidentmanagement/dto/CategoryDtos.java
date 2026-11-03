package br.com.fiap.incidentmanagement.dto;

import br.com.fiap.incidentmanagement.model.Category;
import br.com.fiap.incidentmanagement.model.IncidentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class CategoryDtos {

    private CategoryDtos() {
    }

    public record CategoryRequest(@NotBlank @Size(max = 60) String name) {
    }

    public record CategoryResponse(Long id, String name) {

        public static CategoryResponse from(Category category) {
            return new CategoryResponse(category.getId(), category.getName());
        }
    }

    public record IncidentTypeRequest(
            @NotBlank @Size(max = 60) String name,
            @NotNull Long categoryId) {
    }

    public record IncidentTypeResponse(Long id, String name, Long categoryId, String categoryName) {

        public static IncidentTypeResponse from(IncidentType type) {
            return new IncidentTypeResponse(type.getId(), type.getName(),
                    type.getCategory().getId(), type.getCategory().getName());
        }
    }
}
