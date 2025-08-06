package com.ejemplo.authjwt.security;

import com.ejemplo.authjwt.util.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.stream.Collectors;

public class AuthTokenFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String jwt = parseJwt(request);

            // Solo procesamos el token si existe y es válido
            if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
                logger.info("Token JWT válido encontrado en la solicitud");

                // Extraer el nombre de usuario del token
                String username = jwtUtils.getUserNameFromJwtToken(jwt);
                logger.info("Usuario extraído del token: {}", username);

                // Cargar los detalles del usuario, incluyendo sus roles/autoridades
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                // Log de los roles/autoridades asignados al usuario
                String authorities = userDetails.getAuthorities().stream()
                        .map(auth -> auth.getAuthority())
                        .collect(Collectors.joining(", "));
                logger.info("Roles del usuario {}: {}", username, authorities);

                // Crear el token de autenticación con los detalles del usuario y sus autoridades
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                        );

                // Establecer detalles adicionales de la solicitud
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Establecer la autenticación en el contexto de seguridad
                SecurityContextHolder.getContext().setAuthentication(authentication);
                logger.info("Usuario '{}' autenticado correctamente", username);
            } else if (jwt != null) {
                logger.warn("Token JWT inválido: {}", jwt);
            }
        } catch (Exception e) {
            logger.error("No se pudo establecer la autenticación del usuario: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");

        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }

        return null;
    }
}
