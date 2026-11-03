package br.com.fiap.incidentmanagement.dto;

import br.com.fiap.incidentmanagement.model.Role;
import br.com.fiap.incidentmanagement.model.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class UserDtos {

    private UserDtos() {
    }

    public record UserCreateRequest(
            @NotBlank @Size(max = 100) String name,
            @NotBlank @Size(max = 50) String login,
            @NotBlank @Size(min = 6) String password,
            @NotNull Role role) {
    }

    public record UserUpdateRequest(
            @NotNull Long id,
            @Size(max = 100) String name,
            @Size(max = 50) String login) {
    }

    public record RoleUpdateRequest(
            @NotNull Long id,
            @NotNull Role role) {
    }

    public record PasswordUpdateRequest(
            @NotNull Long id,
            @NotBlank String oldPassword,
            @NotBlank @Size(min = 6) String newPassword) {
    }

    public record UserResponse(Long id, String name, String login, Role role, boolean active) {

        public static UserResponse from(User user) {
            return new UserResponse(user.getId(), user.getName(), user.getLogin(), user.getRole(), user.isActive());
        }
    }
}
