package com.sovereingschool.back_base.Utils;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;

@Component
public class JwtUtil {

    @Value("${security.jwt.private.key}")
    private String privateKay;

    @Value("${security.jwt.user.generator}")
    private String userGenerator;

    private final Date expiredForServer = new Date(System.currentTimeMillis() + 60 * 60 * 1000); // 1 hour
    private final Date expiredForAccessToken = new Date(System.currentTimeMillis() + 15 * 60 * 1000); // 15 minutes
    private final Date expiredForRefreshToken = new Date(System.currentTimeMillis() + 15 * 24 * 60 * 60 * 1000); // 15
                                                                                                                 // days
    private final Date expiredForInitToken = new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000); // 1 dia

    public String generateToken(Authentication authentication, String tokenType, Long id_usuario) {

        Algorithm algorithm = Algorithm.HMAC256(this.privateKay);

        String username = tokenType.equals("server") ? "server" : authentication.getName();
        String roles = tokenType.equals("server") ? "ROLE_ADMIN"
                : authentication.getAuthorities()
                        .stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.joining(","));

        String jwtToken = JWT.create()
                .withIssuer(this.userGenerator)
                .withSubject(username)
                .withClaim("rol", roles)
                .withClaim("id_usuario", id_usuario)
                .withIssuedAt(new Date())
                .withExpiresAt(tokenType == "access" ? this.expiredForAccessToken
                        : tokenType == "server" ? this.expiredForServer : this.expiredForRefreshToken) // 1 hour
                .withJWTId(UUID.randomUUID().toString())
                .withNotBefore(new Date(System.currentTimeMillis())) //
                .sign(algorithm);

        return jwtToken;
    }

    public String generateInitToken() {
        Algorithm algorithm = Algorithm.HMAC256(this.privateKay);
        String jwtToken = JWT.create()
                .withIssuer(this.userGenerator)
                .withSubject("Visitante")
                .withClaim("rol", "ROLE_GUEST")
                .withIssuedAt(new Date())
                .withExpiresAt(this.expiredForInitToken)
                .withJWTId(UUID.randomUUID().toString())
                .withNotBefore(new Date(System.currentTimeMillis())) //
                .sign(algorithm);

        return jwtToken;
    }

    /**
     * Decodes and verifies a JWT token.
     *
     * @param token The JWT token to be decoded and verified.
     * @return DecodedJWT The decoded JWT object containing the token's claims and
     *         payload.
     * @throws JWTVerificationException If the token is invalid or verification
     *                                  fails.
     */
    public DecodedJWT decodeToken(String token) {
        if (token == null || token.isEmpty()) {
            throw new JWTVerificationException("El token no puede estar vacío");
        }

        try {
            Algorithm algorithm = Algorithm.HMAC256(this.privateKay);
            JWTVerifier verifier = JWT.require(algorithm).withIssuer(this.userGenerator).build();
            return verifier.verify(token);
        } catch (JWTVerificationException e) {
            throw new JWTVerificationException("Error al verificar el token: " + e.getMessage());
        } catch (Exception e) {
            throw new JWTVerificationException("Error inesperado al procesar el token: " + e.getMessage());
        }
    }

    public String getUsername(String token) {
        if (token == null || token.isEmpty()) {
            throw new JWTVerificationException("El token no puede estar vacío");
        }

        try {
            DecodedJWT decodedJWT = decodeToken(token);
            String username = decodedJWT.getSubject();
            if (username == null || username.isEmpty()) {
                throw new JWTVerificationException("El token no contiene un nombre de usuario válido");
            }
            return username;
        } catch (JWTVerificationException e) {
            throw new JWTVerificationException("Error al obtener el username: " + e.getMessage());
        }
    }

    public String getRoles(String token) {
        if (token == null || token.isEmpty()) {
            throw new JWTVerificationException("El token no puede estar vacío");
        }

        try {
            DecodedJWT decodedJWT = decodeToken(token);
            String roles = decodedJWT.getClaim("rol").asString();
            if (roles == null || roles.isEmpty()) {
                throw new JWTVerificationException("El token no contiene roles válidos");
            }
            return roles;
        } catch (JWTVerificationException e) {
            throw new JWTVerificationException("Error al obtener los roles: " + e.getMessage());
        }
    }

    public Long getIdUsuario(String token) {
        if (token == null || token.isEmpty()) {
            throw new JWTVerificationException("El token no puede estar vacío");
        }

        try {
            DecodedJWT decodedJWT = decodeToken(token);
            Claim idClaim = decodedJWT.getClaim("id_usuario");
            if (idClaim.isNull()) {
                throw new JWTVerificationException("El token no contiene un ID de usuario válido");
            }
            return idClaim.asLong();
        } catch (JWTVerificationException e) {
            throw new JWTVerificationException("Error al obtener el ID de usuario: " + e.getMessage());
        }
    }

    public String getSpecificClaim(DecodedJWT decodedJWT, String claim) {
        if (decodedJWT == null || claim == null || claim.isEmpty()) {
            throw new IllegalArgumentException("El JWT decodificado y el claim no pueden ser nulos o vacíos");
        }

        try {
            String claimValue = decodedJWT.getClaim(claim).asString();
            if (claimValue == null) {
                throw new JWTVerificationException("El claim solicitado no existe en el token");
            }
            return claimValue;
        } catch (Exception e) {
            throw new JWTVerificationException("Error al obtener el claim específico: " + e.getMessage());
        }
    }

    public Map<String, Claim> getAllClaims(DecodedJWT decodedJWT) {
        return decodedJWT.getClaims();
    }

    public Authentication createAuthenticationFromToken(String token) {
        String username = getUsername(token);
        String rolesString = getRoles(token);

        List<SimpleGrantedAuthority> authorities = Arrays.stream(rolesString.split(","))
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        return new UsernamePasswordAuthenticationToken(
                username,
                null,
                authorities);
    }
}
