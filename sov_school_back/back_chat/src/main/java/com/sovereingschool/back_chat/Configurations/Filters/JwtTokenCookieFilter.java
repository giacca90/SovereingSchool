package com.sovereingschool.back_chat.Configurations.Filters;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.sovereingschool.back_common.Utils.JwtUtil;

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
    protected void doFilterInternal(@NonNull HttpServletRequest req,
            @NonNull HttpServletResponse res,
            @NonNull FilterChain chain)
            throws ServletException, IOException {
        try {
            Cookie[] cookies = req.getCookies();
            if (cookies != null) {
                for (Cookie c : cookies) {
                    if (isValidCookieName(c.getName()) && hasText(c.getValue()) && c.getName().equals("refreshToken")) {
                        Authentication auth = jwtUtil.createAuthenticationFromToken(c.getValue());
                        SecurityContextHolder.getContext().setAuthentication(auth);
                        break;
                    }
                }
            }
            chain.doFilter(req, res);
        } catch (AuthenticationException ex) {
            SecurityContextHolder.clearContext();
            throw new BadCredentialsException("Error en JwtTokenCookieFilter: " + ex.getMessage());
        }
    }

    private boolean isValidCookieName(String name) {
        return "initToken".equals(name) || "refreshToken".equals(name);
    }

    private boolean hasText(String s) {
        return s != null && !s.isBlank();
    }
}
