package com.sovereingschool.back_base.Configurations.Filters;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

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

        // Extraer la cookie
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("initToken") || cookie.getName().equals("refreshToken")) {
                    String token = cookie.getValue();
                    // Aquí deberías validar el token y setear la autenticación
                    Authentication auth = jwtUtil.createAuthenticationFromToken(token);
                    if (auth != null) {
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    }
                    break;
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
