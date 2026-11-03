package br.com.fiap.incidentmanagement.dto;

import br.com.fiap.incidentmanagement.model.Role;
import jakarta.validation.constraints.NotBlank;

public final class AuthDtos {

    private AuthDtos() {
    }

    public record LoginRequest(
            @NotBlank String login,
            @NotBlank String password) {
    }

    public record TokenResponse(String tokenJWT, Long id, String login, String name, Role role) {
    }
}
