package com.ejemplo.authjwt.controller;

import com.ejemplo.authjwt.dto.JwtResponse;
import com.ejemplo.authjwt.dto.LoginRequest;
import com.ejemplo.authjwt.dto.RegisterRequest;
import com.ejemplo.authjwt.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 🔐 Controlador REST para la gestión de autenticación y autorización
 * 
 * <p>Este controlador implementa los endpoints principales para el sistema de autenticación JWT,
 * siguiendo las mejores prácticas de Spring Boot y RESTful API design.</p>
 * 
 * <h3>🎯 Características implementadas:</h3>
 * <ul>
 *   <li>✅ Validación de datos con Jakarta Validation</li>
 *   <li>✅ Manejo de CORS para integración frontend</li>
 *   <li>✅ Respuestas HTTP estandarizadas</li>
 *   <li>✅ Inyección de dependencias con Lombok</li>
 *   <li>✅ Documentación completa con JavaDoc</li>
 * </ul>
 * 
 * <h3>🔒 Seguridad:</h3>
 * <ul>
 *   <li>Validación de entrada para prevenir ataques</li>
 *   <li>Manejo seguro de credenciales</li>
 *   <li>Tokens JWT para autenticación stateless</li>
 * </ul>
 * 
 * @author Alejandro Arango
 * @version 1.0
 * @since 2025-01-01
 * @see AuthService
 * @see JwtResponse
 * @see LoginRequest
 * @see RegisterRequest
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    /**
     * Servicio de autenticación inyectado mediante constructor
     * Utiliza Lombok @RequiredArgsConstructor para inyección automática
     */
    private final AuthService authService;

    /**
     * 🔐 Endpoint para el registro de nuevos usuarios
     * 
     * <p>Permite a los usuarios crear una nueva cuenta en el sistema con validación
     * completa de datos y encriptación segura de contraseñas.</p>
     * 
     * <h4>📋 Validaciones implementadas:</h4>
     * <ul>
     *   <li>✅ Username único en el sistema</li>
     *   <li>✅ Email válido y único</li>
     *   <li>✅ Contraseña con criterios de seguridad</li>
     *   <li>✅ Roles válidos del sistema</li>
     * </ul>
     * 
     * <h4>🔒 Medidas de seguridad:</h4>
     * <ul>
     *   <li>Encriptación BCrypt de contraseñas</li>
     *   <li>Validación de entrada con @Valid</li>
     *   <li>Prevención de ataques de inyección</li>
     * </ul>
     * 
     * @param request DTO con los datos de registro del usuario
     * @return ResponseEntity con mensaje de confirmación
     * @throws IllegalArgumentException si los datos son inválidos
     * @throws RuntimeException si el usuario ya existe
     * 
     * @example
     * <pre>{@code
     * POST /api/auth/register
     * Content-Type: application/json
     * 
     * {
     *   "username": "usuario123",
     *   "email": "usuario@ejemplo.com",
     *   "password": "Contraseña123!",
     *   "roles": ["ROLE_USER"]
     * }
     * }</pre>
     */
    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    /**
     * 🔑 Endpoint para autenticación de usuarios
     * 
     * <p>Permite a los usuarios autenticarse en el sistema y recibe un token JWT
     * para acceder a recursos protegidos.</p>
     * 
     * <h4>⚡ Proceso de autenticación:</h4>
     * <ol>
     *   <li>Validación de credenciales</li>
     *   <li>Verificación de contraseña con BCrypt</li>
     *   <li>Generación de token JWT</li>
     *   <li>Retorno de datos del usuario y token</li>
     * </ol>
     * 
     * <h4>🔐 Respuesta incluye:</h4>
     * <ul>
     *   <li>Token JWT para autenticación</li>
     *   <li>Datos del usuario autenticado</li>
     *   <li>Roles y permisos asignados</li>
     *   <li>Información de expiración del token</li>
     * </ul>
     * 
     * @param request DTO con credenciales de autenticación
     * @return ResponseEntity con token JWT y datos del usuario
     * @throws AuthenticationException si las credenciales son inválidas
     * @throws RuntimeException si el usuario no existe
     * 
     * @example
     * <pre>{@code
     * POST /api/auth/login
     * Content-Type: application/json
     * 
     * {
     *   "username": "usuario123",
     *   "password": "Contraseña123!"
     * }
     * 
     * Response:
     * {
     *   "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
     *   "type": "Bearer",
     *   "id": 1,
     *   "username": "usuario123",
     *   "email": "usuario@ejemplo.com",
     *   "roles": ["ROLE_USER"]
     * }
     * }</pre>
     */
    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
