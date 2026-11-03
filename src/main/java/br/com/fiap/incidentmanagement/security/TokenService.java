package br.com.fiap.incidentmanagement.security;

import br.com.fiap.incidentmanagement.model.User;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
public class TokenService {

    private static final String ISSUER = "incident-management-api";

    private final Algorithm algorithm;
    private final long expirationHours;

    public TokenService(@Value("${api.security.token.secret}") String secret,
                        @Value("${api.security.token.expiration-hours}") long expirationHours) {
        this.algorithm = Algorithm.HMAC256(secret);
        this.expirationHours = expirationHours;
    }

    public String generate(User user) {
        return JWT.create()
                .withIssuer(ISSUER)
                .withSubject(user.getLogin())
                .withClaim("id", user.getId())
                .withClaim("role", user.getRole().name())
                .withExpiresAt(Instant.now().plus(expirationHours, ChronoUnit.HOURS))
                .sign(algorithm);
    }

    /**
     * @return o login (subject) do token, ou vazio se o token for inválido/expirado.
     */
    public Optional<String> getSubject(String token) {
        try {
            return Optional.of(JWT.require(algorithm).withIssuer(ISSUER).build().verify(token).getSubject());
        } catch (JWTVerificationException e) {
            return Optional.empty();
        }
    }
}
