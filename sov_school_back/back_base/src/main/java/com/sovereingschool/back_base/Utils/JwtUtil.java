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

    private final Date expiredForServer = new Date(System.currentTimeMillis() + 3600000); // 1 hour
    private final Date expiredForAccessToken = new Date(System.currentTimeMillis() + 900000); // 15 minutes
    private final Date expiredForRefreshToken = new Date(System.currentTimeMillis() + 1296000000); // 15 days
    private final Date expiredForInitToken = new Date(System.currentTimeMillis() + 15000); // 15 seconds

    public String generateToken(Authentication authentication, String tokenType) {

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
                .withIssuedAt(new Date())
                .withExpiresAt(tokenType == "access" ? this.expiredForAccessToken
                        : tokenType == "server" ? this.expiredForServer : this.expiredForRefreshToken) // 1 hour
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

    public String generateInitToken() {
        Algorithm algorithm = Algorithm.HMAC256(this.privateKay);
        String jwtToken = JWT.create()
                .withIssuer(this.userGenerator)
                .withSubject("Visitante")
                .withClaim("rol", "ROLE_GUEST")
                .withIssuedAt(new Date())
                .withExpiresAt(this.expiredForInitToken) // 1 hour
                .withJWTId(UUID.randomUUID().toString())
                .withNotBefore(new Date(System.currentTimeMillis())) //
                .sign(algorithm);

        return jwtToken;
    }

    public Authentication createAuthenticationFromToken(String token) {
        DecodedJWT decodedJWT = decodeToken(token);
        String username = getUsername(decodedJWT);
        String rolesString = getRoles(decodedJWT);

        List<SimpleGrantedAuthority> authorities = Arrays.stream(rolesString.split(","))
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        return new UsernamePasswordAuthenticationToken(
                username,
                null,
                authorities);
    }
}
