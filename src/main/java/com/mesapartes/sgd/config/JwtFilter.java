package com.mesapartes.sgd.config;

import com.mesapartes.sgd.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtFilter.class);

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        try {

            String authHeader = request.getHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                chain.doFilter(request, response);
                return;
            }

            String token = authHeader.substring(7);

            JwtService.TokenValidationResult result = jwtService.validarToken(token);

            if (result != JwtService.TokenValidationResult.VALID) {

                log.warn("[JWT] Token {} desde IP={} path={}",
                        result,
                        request.getRemoteAddr(),
                        request.getRequestURI());

                SecurityContextHolder.clearContext();

                writeUnauthorized(response, result);
                return;
            }

            if (SecurityContextHolder.getContext().getAuthentication() == null) {

                String email = jwtService.obtenerEmail(token);
                String rol = jwtService.obtenerRol(token);

                if (email == null) {
                    SecurityContextHolder.clearContext();
                    writeUnauthorized(response, JwtService.TokenValidationResult.INVALID);
                    return;
                }

                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(
                                email,
                                null,
                                List.of(new SimpleGrantedAuthority("ROLE_" + rol))
                        );

                auth.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                SecurityContextHolder.getContext().setAuthentication(auth);
            }

            chain.doFilter(request, response);

        } catch (Exception ex) {

            log.error("[JWT] Error procesando token", ex);

            SecurityContextHolder.clearContext();
            writeUnauthorized(response, JwtService.TokenValidationResult.INVALID);
        }
    }

    private void writeUnauthorized(HttpServletResponse response,
                                   JwtService.TokenValidationResult result)
            throws IOException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");

        String msg = result == JwtService.TokenValidationResult.EXPIRED
                ? "Token expirado"
                : "Token inválido";

        response.getWriter().write(
                "{\"error\":\"" + msg + "\",\"status\":401}"
        );
    }
}