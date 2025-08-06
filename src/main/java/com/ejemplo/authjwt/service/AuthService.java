package com.ejemplo.authjwt.service;

import com.ejemplo.authjwt.dto.JwtResponse;
import com.ejemplo.authjwt.dto.LoginRequest;
import com.ejemplo.authjwt.dto.RegisterRequest;
import com.ejemplo.authjwt.entity.Role;
import com.ejemplo.authjwt.entity.User;
import com.ejemplo.authjwt.repository.RoleRepository;
import com.ejemplo.authjwt.repository.UserRepository;
import com.ejemplo.authjwt.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Servicio que maneja la lógica de autenticación y registro
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    /**
     * Registra un nuevo usuario en el sistema
     * @param request Datos del usuario a registrar
     * @return Mensaje de confirmación
     * @throws RuntimeException si el username o email ya existen
     */
    @Transactional
    public String register(RegisterRequest request) {
        // Validación de usuario existente
        validateNewUser(request);

        // Crear nuevo usuario
        User user = createUserFromRequest(request);

        // Asignar roles
        assignRoles(user, request.getRoles());

        // Guardar usuario
        userRepository.save(user);

        return "Usuario registrado exitosamente";
    }

    /**
     * Autentica un usuario y genera un token JWT
     * @param request Credenciales de login
     * @return Respuesta con token JWT y datos del usuario
     */
    public JwtResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(request.getUsername());

        User user = userRepository.findByUsername(request.getUsername())
            .orElseThrow(() -> new RuntimeException("Error: Usuario no encontrado"));

        List<String> roles = user.getRoles().stream()
            .map(Role::getName)
            .collect(Collectors.toList());

        return new JwtResponse(jwt, user.getUsername(), user.getEmail(), roles);
    }

    /**
     * Valida que el usuario no exista previamente
     */
    private void validateNewUser(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("El nombre de usuario ya existe");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("El email ya está registrado");
        }
    }

    /**
     * Crea una nueva entidad User a partir de los datos de registro
     */
    private User createUserFromRequest(RegisterRequest request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return user;
    }

    /**
     * Asigna roles al usuario
     */
    private void assignRoles(User user, Set<String> requestRoles) {
        Set<Role> roles = new HashSet<>();

        if (requestRoles == null || requestRoles.isEmpty()) {
            Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Error: Role ROLE_USER no encontrado"));
            roles.add(userRole);
        } else {
            requestRoles.forEach(roleName -> {
                Role role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> new RuntimeException("Error: Role " + roleName + " no encontrado"));
                roles.add(role);
            });
        }

        user.setRoles(roles);
    }
}
