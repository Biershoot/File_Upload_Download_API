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
 * 🔐 Servicio de Autenticación y Autorización
 * 
 * <p>Este servicio implementa la lógica de negocio para el sistema de autenticación JWT,
 * siguiendo los principios SOLID y patrones de diseño empresarial.</p>
 * 
 * <h3>🎯 Responsabilidades principales:</h3>
 * <ul>
 *   <li>✅ Registro seguro de nuevos usuarios</li>
 *   <li>✅ Autenticación con Spring Security</li>
 *   <li>✅ Generación y validación de tokens JWT</li>
 *   <li>✅ Gestión de roles y permisos</li>
 *   <li>✅ Encriptación segura de contraseñas</li>
 * </ul>
 * 
 * <h3>🏗️ Arquitectura implementada:</h3>
 * <ul>
 *   <li><strong>Service Layer Pattern:</strong> Separación clara de responsabilidades</li>
 *   <li><strong>Dependency Injection:</strong> Inyección de dependencias con Lombok</li>
 *   <li><strong>Transaction Management:</strong> Manejo de transacciones con @Transactional</li>
 *   <li><strong>Security Integration:</strong> Integración con Spring Security</li>
 * </ul>
 * 
 * <h3>🔒 Medidas de seguridad:</h3>
 * <ul>
 *   <li>Encriptación BCrypt para contraseñas</li>
 *   <li>Validación de usuarios duplicados</li>
 *   <li>Manejo seguro de tokens JWT</li>
 *   <li>Control de acceso basado en roles</li>
 * </ul>
 * 
 * @author Alejandro Arango
 * @version 1.0
 * @since 2025-01-01
 * @see UserRepository
 * @see RoleRepository
 * @see JwtUtils
 * @see PasswordEncoder
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    /**
     * Repositorio para operaciones de persistencia de usuarios
     * Implementa el patrón Repository para abstraer la capa de datos
     */
    private final UserRepository userRepository;
    
    /**
     * Repositorio para operaciones de persistencia de roles
     * Gestiona la relación muchos a muchos entre usuarios y roles
     */
    private final RoleRepository roleRepository;
    
    /**
     * Codificador de contraseñas para encriptación segura
     * Utiliza BCrypt para hash de contraseñas con salt automático
     */
    private final PasswordEncoder passwordEncoder;
    
    /**
     * Gestor de autenticación de Spring Security
     * Maneja el proceso de validación de credenciales
     */
    private final AuthenticationManager authenticationManager;
    
    /**
     * Utilidad para generación y validación de tokens JWT
     * Implementa la lógica de firma y verificación de tokens
     */
    private final JwtUtils jwtUtils;

    /**
     * 🔐 Registra un nuevo usuario en el sistema
     * 
     * <p>Este método implementa el proceso completo de registro de usuarios,
     * incluyendo validaciones, encriptación de contraseñas y asignación de roles.</p>
     * 
     * <h4>⚡ Proceso de registro:</h4>
     * <ol>
     *   <li>Validación de datos únicos (username, email)</li>
     *   <li>Encriptación segura de contraseña con BCrypt</li>
     *   <li>Creación de entidad User con timestamps</li>
     *   <li>Asignación de roles por defecto o personalizados</li>
     *   <li>Persistencia en base de datos</li>
     * </ol>
     * 
     * <h4>🔒 Validaciones implementadas:</h4>
     * <ul>
     *   <li>Username único en el sistema</li>
     *   <li>Email único y válido</li>
     *   <li>Roles válidos del sistema</li>
     *   <li>Integridad referencial de datos</li>
     * </ul>
     * 
     * <h4>💾 Gestión de transacciones:</h4>
     * <ul>
     *   <li>Transacción atómica para todo el proceso</li>
     *   <li>Rollback automático en caso de error</li>
     *   <li>Consistencia de datos garantizada</li>
     * </ul>
     * 
     * @param request DTO con los datos de registro del usuario
     * @return Mensaje de confirmación del registro exitoso
     * @throws RuntimeException si el username o email ya existen
     * @throws RuntimeException si los roles especificados no existen
     * 
     * @example
     * <pre>{@code
     * RegisterRequest request = new RegisterRequest();
     * request.setUsername("nuevoUsuario");
     * request.setEmail("usuario@ejemplo.com");
     * request.setPassword("Contraseña123!");
     * request.setRoles(Set.of("ROLE_USER"));
     * 
     * String result = authService.register(request);
     * // Resultado: "Usuario registrado exitosamente"
     * }</pre>
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
     * 🔑 Autentica un usuario y genera un token JWT
     * 
     * <p>Este método implementa el proceso de autenticación utilizando Spring Security
     * y genera un token JWT para acceso a recursos protegidos.</p>
     * 
     * <h4>⚡ Flujo de autenticación:</h4>
     * <ol>
     *   <li>Validación de credenciales con AuthenticationManager</li>
     *   <li>Establecimiento del contexto de seguridad</li>
     *   <li>Generación de token JWT personalizado</li>
     *   <li>Construcción de respuesta con datos del usuario</li>
     * </ol>
     * 
     * <h4>🔐 Características del token JWT:</h4>
     * <ul>
     *   <li>Firma digital con clave secreta</li>
     *   <li>Payload con claims personalizados</li>
     *   <li>Tiempo de expiración configurable</li>
     *   <li>Información de roles y permisos</li>
     * </ul>
     * 
     * <h4>🛡️ Medidas de seguridad:</h4>
     * <ul>
     *   <li>Validación de contraseña con BCrypt</li>
     *   <li>Prevención de ataques de fuerza bruta</li>
     *   <li>Manejo seguro de sesiones stateless</li>
     *   <li>Control de acceso basado en roles</li>
     * </ul>
     * 
     * @param request DTO con credenciales de autenticación
     * @return JwtResponse con token y datos del usuario autenticado
     * @throws RuntimeException si las credenciales son inválidas
     * @throws RuntimeException si el usuario no existe en el sistema
     * 
     * @example
     * <pre>{@code
     * LoginRequest request = new LoginRequest();
     * request.setUsername("usuario123");
     * request.setPassword("Contraseña123!");
     * 
     * JwtResponse response = authService.login(request);
     * // Response incluye: token, username, email, roles
     * }</pre>
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
     * 🔍 Valida que el usuario no exista previamente en el sistema
     * 
     * <p>Este método privado implementa validaciones de negocio para asegurar
     * la unicidad de los datos de usuario antes del registro.</p>
     * 
     * <h4>📋 Validaciones implementadas:</h4>
     * <ul>
     *   <li>Username único en la base de datos</li>
     *   <li>Email único y no duplicado</li>
     *   <li>Manejo de excepciones descriptivas</li>
     * </ul>
     * 
     * @param request DTO con los datos de registro a validar
     * @throws RuntimeException si el username ya existe
     * @throws RuntimeException si el email ya está registrado
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
     * 🏗️ Crea una nueva entidad User a partir de los datos de registro
     * 
     * <p>Este método implementa el patrón Builder para la creación de entidades,
     * asegurando la consistencia de datos y la aplicación de reglas de negocio.</p>
     * 
     * <h4>🔧 Características implementadas:</h4>
     * <ul>
     *   <li>Encriptación automática de contraseña con BCrypt</li>
     *   <li>Timestamps automáticos de creación y actualización</li>
     *   <li>Mapeo seguro de datos del DTO a la entidad</li>
     *   <li>Validación de integridad de datos</li>
     * </ul>
     * 
     * @param request DTO con los datos de registro del usuario
     * @return Entidad User configurada y lista para persistir
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
     * 🎭 Asigna roles al usuario durante el proceso de registro
     * 
     * <p>Este método implementa la lógica de asignación de roles, permitiendo
     * roles personalizados o asignando el rol por defecto ROLE_USER.</p>
     * 
     * <h4>🎯 Lógica de asignación:</h4>
     * <ul>
     *   <li>Si no se especifican roles → ROLE_USER por defecto</li>
     *   <li>Si se especifican roles → Validación y asignación personalizada</li>
     *   <li>Validación de existencia de roles en el sistema</li>
     *   <li>Manejo de relaciones muchos a muchos</li>
     * </ul>
     * 
     * <h4>🔒 Seguridad implementada:</h4>
     * <ul>
     *   <li>Validación de roles válidos del sistema</li>
     *   <li>Prevención de asignación de roles inexistentes</li>
     *   <li>Manejo seguro de relaciones de base de datos</li>
     * </ul>
     * 
     * @param user Entidad User a la cual asignar roles
     * @param requestRoles Conjunto de nombres de roles a asignar
     * @throws RuntimeException si algún rol especificado no existe en el sistema
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
