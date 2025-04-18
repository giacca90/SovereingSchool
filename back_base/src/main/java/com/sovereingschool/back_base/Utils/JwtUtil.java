package com.sovereingschool.back_base.Utils;

import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
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

    public String generateToken(Authentication authentication) {
        Algorithm algorithm = Algorithm.HMAC256(this.privateKay);

        String username = authentication.getName();

        String roles = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        String jwtToken = JWT.create()
                .withIssuer(this.userGenerator)
                .withSubject(username)
                .withClaim("rol", roles)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + 3600000)) // 1 hour
                .withJWTId(UUID.randomUUID().toString())
                .withNotBefore(new Date(System.currentTimeMillis())) //
                .sign(algorithm);

        return jwtToken;
    }

    public String generateTokenForServer() {
        Algorithm algorithm = Algorithm.HMAC256(this.privateKay);

        String jwtToken = JWT.create()
                .withIssuer(this.userGenerator)
                .withSubject("server")
                .withIssuedAt(new Date())
                .withClaim("rol", "ROLE_ADMIN")
                .withClaim("server", true)
                .withExpiresAt(new Date(System.currentTimeMillis() + 3600000)) // 1 hour
                .withJWTId(UUID.randomUUID().toString())
                .withNotBefore(new Date(System.currentTimeMillis())) //
                .sign(algorithm);

        return jwtToken;

    }

    public DecodedJWT decodeToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(this.privateKay);
            JWTVerifier verifier = JWT.require(algorithm).withIssuer(this.userGenerator).build();
            DecodedJWT decodedJWT = verifier.verify(token);
            return decodedJWT;
        } catch (JWTVerificationException exception) {
            throw new JWTVerificationException("Token invalido");
        }

    }

    public String getUsername(DecodedJWT decodedJWT) {
        return decodedJWT.getSubject().toString();
    }

    public String getRoles(DecodedJWT decodedJWT) {
        return decodedJWT.getClaim("rol").asString();
    }

    public String getSpecificClaim(DecodedJWT decodedJWT, String claim) {
        return decodedJWT.getClaim(claim).asString();
    }

    public Map<String, Claim> getAllClaims(DecodedJWT decodedJWT) {
        return decodedJWT.getClaims();
    }
}
