package br.com.fiap.incidentmanagement.controller;

import br.com.fiap.incidentmanagement.dto.AuthDtos.LoginRequest;
import br.com.fiap.incidentmanagement.dto.AuthDtos.TokenResponse;
import br.com.fiap.incidentmanagement.model.User;
import br.com.fiap.incidentmanagement.security.TokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/login")
@Tag(name = "Autenticação")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;

    public AuthController(AuthenticationManager authenticationManager, TokenService tokenService) {
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
    }

    @PostMapping
    @Operation(summary = "Autentica o usuário e devolve o token JWT (RF01)")
    public TokenResponse login(@RequestBody @Valid LoginRequest request) {
        var authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.login(), request.password()));
        User user = (User) authentication.getPrincipal();
        return new TokenResponse(tokenService.generate(user), user.getId(), user.getLogin(), user.getName(),
                user.getRole());
    }
}
