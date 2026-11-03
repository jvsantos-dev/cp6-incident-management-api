package br.com.fiap.incidentmanagement.security;

import br.com.fiap.incidentmanagement.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Lê o token JWT do header Authorization e autentica o usuário na requisição.
 */
@Component
public class SecurityFilter extends OncePerRequestFilter {

    private final TokenService tokenService;
    private final UserRepository userRepository;

    public SecurityFilter(TokenService tokenService, UserRepository userRepository) {
        this.tokenService = tokenService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            tokenService.getSubject(header.substring(7))
                    .flatMap(userRepository::findByLogin)
                    .filter(user -> user.isActive())
                    .ifPresent(user -> SecurityContextHolder.getContext().setAuthentication(
                            new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities())));
        }
        chain.doFilter(request, response);
    }
}
