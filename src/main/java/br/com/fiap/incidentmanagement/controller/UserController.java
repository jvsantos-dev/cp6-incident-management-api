package br.com.fiap.incidentmanagement.controller;

import br.com.fiap.incidentmanagement.dto.PageResponse;
import br.com.fiap.incidentmanagement.dto.UserDtos.*;
import br.com.fiap.incidentmanagement.model.Role;
import br.com.fiap.incidentmanagement.model.User;
import br.com.fiap.incidentmanagement.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/users")
@Tag(name = "Usuários")
@SecurityRequirement(name = "bearer-key")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @Operation(summary = "Cadastra um usuário")
    public ResponseEntity<UserResponse> create(@RequestBody @Valid UserCreateRequest request,
                                               UriComponentsBuilder uriBuilder) {
        UserResponse user = userService.create(request);
        return ResponseEntity.created(uriBuilder.path("/users/{id}").buildAndExpand(user.id()).toUri()).body(user);
    }

    @GetMapping
    @Operation(summary = "Lista os usuários ativos (paginado)")
    public PageResponse<UserResponse> list(@PageableDefault(size = 10, sort = "id") Pageable pageable) {
        return PageResponse.of(userService.list(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulta um usuário pelo id")
    public UserResponse findById(@PathVariable Long id) {
        return userService.findById(id);
    }

    @GetMapping("/roles")
    @Operation(summary = "Lista os perfis disponíveis")
    public List<String> roles() {
        return Arrays.stream(Role.values()).map(Enum::name).toList();
    }

    @PutMapping
    @Operation(summary = "Atualiza nome e login do usuário")
    public UserResponse update(@RequestBody @Valid UserUpdateRequest request) {
        return userService.update(request);
    }

    @PatchMapping
    @Operation(summary = "Altera o perfil do usuário")
    public UserResponse updateRole(@RequestBody @Valid RoleUpdateRequest request) {
        return userService.updateRole(request);
    }

    @PatchMapping("/password")
    @Operation(summary = "Altera a senha do usuário autenticado")
    public ResponseEntity<Void> updatePassword(@RequestBody @Valid PasswordUpdateRequest request,
                                               @AuthenticationPrincipal User authenticated) {
        userService.updatePassword(request, authenticated);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Inativa o usuário (exclusão lógica)")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.inactivate(id);
        return ResponseEntity.noContent().build();
    }
}
