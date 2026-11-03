package br.com.fiap.incidentmanagement.security;

import br.com.fiap.incidentmanagement.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Regras de acesso por perfil (RF01 e item 1 dos requisitos):
 * <ul>
 *     <li>PROFESSOR: registra, altera e exclui as próprias ocorrências; consulta todas.</li>
 *     <li>ANALISTA: consulta todas as ocorrências e o dashboard.</li>
 *     <li>COORDENADOR: dashboard, consultas completas e acompanhamento do status.</li>
 *     <li>ADMINISTRATIVO: turmas, alunos, vínculo aluno-turma, categorias, tipos e usuários.</li>
 * </ul>
 */
@Configuration
public class SecurityConfig {

    private static final String PROFESSOR = "PROFESSOR";
    private static final String ANALISTA = "ANALISTA";
    private static final String COORDENADOR = "COORDENADOR";
    private static final String ADMINISTRATIVO = "ADMINISTRATIVO";

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, SecurityFilter securityFilter) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .authorizeHttpRequests(auth -> auth
                        // públicos: login, documentação, console H2, frontend estático e fotos
                        .requestMatchers(HttpMethod.POST, "/login").permitAll()
                        .requestMatchers(PathPatternRequestMatcher.withDefaults().matcher("/h2-console/**")).permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/error").permitAll()
                        .requestMatchers(HttpMethod.GET, "/", "/index.html", "/static/**", "/templates/**",
                                "/favicon.ico", "/students/*/image").permitAll()

                        // usuários
                        .requestMatchers(HttpMethod.PATCH, "/users/password").authenticated()
                        .requestMatchers(HttpMethod.GET, "/users/roles").authenticated()
                        .requestMatchers("/users/**", "/users").hasRole(ADMINISTRATIVO)

                        // cadastros do administrativo: leitura liberada para qualquer perfil autenticado
                        .requestMatchers(HttpMethod.GET, "/students/**", "/schoolclasses/**", "/categories/**",
                                "/types/**").authenticated()
                        .requestMatchers("/students/**", "/students", "/schoolclasses/**", "/schoolclasses",
                                "/categories/**", "/categories", "/types/**", "/types").hasRole(ADMINISTRATIVO)

                        // ocorrências
                        .requestMatchers(HttpMethod.GET, "/incidents/summary").hasAnyRole(COORDENADOR, ANALISTA)
                        .requestMatchers(HttpMethod.PATCH, "/incidents/status").hasAnyRole(COORDENADOR, ANALISTA)
                        .requestMatchers(HttpMethod.GET, "/incidents/**", "/incidents").authenticated()
                        .requestMatchers(HttpMethod.POST, "/incidents").hasRole(PROFESSOR)
                        .requestMatchers(HttpMethod.PUT, "/incidents").hasRole(PROFESSOR)
                        .requestMatchers(HttpMethod.DELETE, "/incidents/**").hasRole(PROFESSOR)

                        .anyRequest().authenticated())
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public UserDetailsService userDetailsService(UserRepository userRepository) {
        return login -> userRepository.findByLogin(login)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
