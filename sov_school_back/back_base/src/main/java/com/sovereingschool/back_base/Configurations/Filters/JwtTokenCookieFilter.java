package com.sovereingschool.back_base.Configurations.Filters;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.sovereingschool.back_base.Utils.JwtUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtTokenCookieFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenCookieFilter.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        try {
            processCookies(request);
        } catch (TokenExpiredException e) {
            logger.warn("Token expirado: {}", e.getMessage());
            clearSecurityContext();
        } catch (JWTVerificationException e) {
            logger.error("Error al verificar token: {}", e.getMessage());
            clearSecurityContext();
        } catch (Exception e) {
            logger.error("Error inesperado en el filtro JWT: {}", e.getMessage());
            clearSecurityContext();
        }

        filterChain.doFilter(request, response);
    }

    private void processCookies(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            logger.debug("No se encontraron cookies en la petición");
            return;
        }

        for (Cookie cookie : cookies) {
            if (isValidCookieName(cookie.getName())) {
                processCookie(cookie);
                break;
            }
        }
    }

    private boolean isValidCookieName(String cookieName) {
        return cookieName.equals("initToken") || cookieName.equals("refreshToken");
    }

    private void processCookie(Cookie cookie) {
        if (cookie.getValue() == null || cookie.getValue().isEmpty()) {
            logger.warn("Cookie {} encontrada pero sin valor", cookie.getName());
            return;
        }

        try {
            String token = cookie.getValue();
            Authentication auth = jwtUtil.createAuthenticationFromToken(token);
            if (auth != null) {
                SecurityContextHolder.getContext().setAuthentication(auth);
                logger.debug("Autenticación establecida exitosamente para el usuario");
            }
        } catch (Exception e) {
            logger.error("Error procesando cookie {}: {}", cookie.getName(), e.getMessage());
            throw e;
        }
    }

    private void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }
}
