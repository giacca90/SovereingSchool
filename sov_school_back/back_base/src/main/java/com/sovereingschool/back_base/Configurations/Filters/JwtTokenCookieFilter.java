package com.sovereingschool.back_base.Configurations.Filters;

import java.io.IOException;

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

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        try {
            processCookies(request);
        } catch (TokenExpiredException e) {
            request.setAttribute("customErrorMessage", "Token expirado: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("text/plain;charset=UTF-8");
            response.getWriter().write("Token expirado: " + e.getMessage());
        } catch (JWTVerificationException e) {
            request.setAttribute("customErrorMessage", "Error al verificar token: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("text/plain;charset=UTF-8");
            response.getWriter().write("Error al verificar token: " + e.getMessage());
        } catch (Exception e) {
            request.setAttribute("customErrorMessage", "Error inesperado en el filtro JWT: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("text/plain;charset=UTF-8");
            response.getWriter().write("Error inesperado en el filtro JWT: " + e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private void processCookies(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null)
            return;

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
        if (cookie.getValue() == null || cookie.getValue().isEmpty())
            return;

        String token = cookie.getValue();
        Authentication auth = jwtUtil.createAuthenticationFromToken(token);
        if (auth != null) {
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
    }
}
