package com.ejemplo.authjwt.service;

import com.ejemplo.authjwt.dto.JwtResponse;
import com.ejemplo.authjwt.dto.LoginRequest;
import com.ejemplo.authjwt.dto.RegisterRequest;
import com.ejemplo.authjwt.dto.SignupRequest;
import com.ejemplo.authjwt.entity.Role;
import com.ejemplo.authjwt.entity.User;
import com.ejemplo.authjwt.repository.RoleRepository;
import com.ejemplo.authjwt.repository.UserRepository;
import com.ejemplo.authjwt.security.UserPrincipal;
import com.ejemplo.authjwt.util.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthService {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    public ResponseEntity<?> authenticateUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        List<String> roles = userPrincipal.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        return ResponseEntity.ok(new JwtResponse(jwt,
                userPrincipal.getId(),
                userPrincipal.getUsername(),
                userPrincipal.getEmail(),
                roles));
    }

    public ResponseEntity<?> registerUser(SignupRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body("Error: Username is already taken!");
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body("Error: Email is already in use!");
        }

        // Create new user's account
        User user = new User();
        user.setUsername(signUpRequest.getUsername());
        user.setEmail(signUpRequest.getEmail());
        user.setPassword(encoder.encode(signUpRequest.getPassword()));

        Set<String> strRoles = signUpRequest.getRole();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null) {
            Role userRole = roleRepository.findByName(Role.RoleName.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(userRole);
        } else {
            strRoles.forEach(role -> {
                switch (role) {
                    case "admin":
                        Role adminRole = roleRepository.findByName(Role.RoleName.ROLE_ADMIN)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(adminRole);
                        break;
                    default:
                        Role userRole = roleRepository.findByName(Role.RoleName.ROLE_USER)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(userRole);
                }
            });
        }

        user.setRoles(roles);
        userRepository.save(user);

        return ResponseEntity.ok("User registered successfully!");
    }

    public String register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("El nombre de usuario ya existe");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("El email ya está registrado");
        }

        Set<Role> userRoles = new HashSet<>();

        if (request.getRoles() == null || request.getRoles().isEmpty()) {
            // Asignar por defecto ROLE_USER
            Role userRole = roleRepository.findByName(Role.RoleName.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Rol no encontrado"));
            userRoles.add(userRole);
        } else {
            request.getRoles().forEach(roleName -> {
                switch (roleName) {
                    case "admin":
                        Role adminRole = roleRepository.findByName(Role.RoleName.ROLE_ADMIN)
                                .orElseThrow(() -> new RuntimeException("Rol no encontrado: " + roleName));
                        userRoles.add(adminRole);
                        break;
                    default:
                        Role userRole = roleRepository.findByName(Role.RoleName.ROLE_USER)
                                .orElseThrow(() -> new RuntimeException("Rol no encontrado: " + roleName));
                        userRoles.add(userRole);
                }
            });
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(encoder.encode(request.getPassword()));
        user.setRoles(userRoles);

        userRepository.save(user);

        return "Usuario registrado exitosamente";
    }

    public JwtResponse login(LoginRequest request) {
        // Validar que se proporcionen nombre de usuario y contraseña
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            throw new RuntimeException("El nombre de usuario es obligatorio");
        }

        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new RuntimeException("La contraseña es obligatoria");
        }

        // Buscar usuario - imprimir información de depuración
        System.out.println("🔍 Buscando usuario: " + request.getUsername());

        // Verificar si el usuario existe
        boolean userExists = userRepository.existsByUsername(request.getUsername());
        if (!userExists) {
            System.out.println("❌ Usuario no encontrado: " + request.getUsername());
            throw new RuntimeException("Usuario no encontrado: " + request.getUsername());
        }

        // Obtener el usuario
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Error al recuperar el usuario de la base de datos"));

        // Verificar la contraseña
        if (!encoder.matches(request.getPassword(), user.getPassword())) {
            System.out.println("❌ Contraseña incorrecta para usuario: " + request.getUsername());
            throw new RuntimeException("Contraseña incorrecta");
        }

        System.out.println("✅ Autenticación exitosa para usuario: " + request.getUsername());

        // Convertir roles a string separados por coma
        String roles = user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.joining(","));

        System.out.println("🔑 Generando token para usuario: " + request.getUsername() + " con roles: " + roles);

        String token = jwtUtils.generateTokenFromUsername(user.getUsername());

        System.out.println("✅ Token generado correctamente");

        return new JwtResponse(token);
    }
}
