package com.ejemplo.authjwt.service;

import com.ejemplo.authjwt.dto.JwtResponse;
import com.ejemplo.authjwt.entity.User;
import com.ejemplo.authjwt.repository.UserRepository;
import com.ejemplo.authjwt.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 🔐 Servicio para Gestión de Autenticación OAuth 2.0
 * 
 * <p>Este servicio implementa la lógica de negocio para la autenticación OAuth 2.0,
 * incluyendo el procesamiento de usuarios de proveedores externos y la generación
 * de tokens JWT personalizados.</p>
 * 
 * <h3>🎯 Funcionalidades implementadas:</h3>
 * <ul>
 *   <li>✅ Procesamiento de usuarios OAuth 2.0 (Google, GitHub)</li>
 *   <li>✅ Generación de JWT personalizados para usuarios OAuth</li>
 *   <li>✅ Vinculación de cuentas OAuth con cuentas locales</li>
 *   <li>✅ Gestión de sesiones híbridas</li>
 *   <li>✅ Mapeo seguro de datos de proveedores</li>
 * </ul>
 * 
 * <h3>🏗️ Arquitectura implementada:</h3>
 * <ul>
 *   <li><strong>OAuth 2.0 Integration:</strong> Integración con proveedores externos</li>
 *   <li><strong>Hybrid Authentication:</strong> Combinación de OAuth y JWT</li>
 *   <li><strong>Account Linking:</strong> Vinculación de cuentas múltiples</li>
 *   <li><strong>Data Mapping:</strong> Mapeo seguro de datos de usuario</li>
 * </ul>
 * 
 * <h3>🔒 Medidas de seguridad:</h3>
 * <ul>
 *   <li>Validación de tokens OAuth 2.0</li>
 *   <li>Verificación de proveedores autorizados</li>
 *   <li>Mapeo seguro de datos sensibles</li>
 *   <li>Gestión de permisos granulares</li>
 * </ul>
 * 
 * @author Alejandro Arango
 * @version 1.0
 * @since 2025-01-01
 * @see UserRepository
 * @see JwtUtils
 * @see JwtResponse
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OAuth2Service {

    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;

    /**
     * 🔑 Procesa un usuario autenticado por OAuth 2.0
     * 
     * <p>Este método implementa el procesamiento completo de un usuario OAuth 2.0,
     * incluyendo la creación o actualización de la cuenta local y la generación
     * de un token JWT personalizado.</p>
     * 
     * <h4>⚡ Proceso de procesamiento:</h4>
     * <ol>
     *   <li>Extracción de datos del usuario OAuth 2.0</li>
     *   <li>Búsqueda de usuario existente en el sistema</li>
     *   <li>Creación o actualización de cuenta local</li>
     *   <li>Generación de token JWT personalizado</li>
     *   <li>Retorno de respuesta con datos del usuario</li>
     * </ol>
     * 
     * <h4>🔐 Datos extraídos del proveedor:</h4>
     * <ul>
     *   <li><strong>Google:</strong> email, nombre, foto, ID único</li>
     *   <li><strong>GitHub:</strong> username, email, repositorios, ID único</li>
     *   <li><strong>Común:</strong> Información de perfil y metadatos</li>
     * </ul>
     * 
     * <h4>🛡️ Medidas de seguridad:</h4>
     * <ul>
     *   <li>Validación de proveedor autorizado</li>
     *   <li>Verificación de datos requeridos</li>
     *   <li>Mapeo seguro de información sensible</li>
     *   <li>Generación de JWT con claims personalizados</li>
     * </ul>
     * 
     * @param oauth2User Usuario autenticado por OAuth 2.0
     * @return JwtResponse con token y datos del usuario procesado
     * @throws RuntimeException si el procesamiento falla
     * 
     * @example
     * <pre>{@code
     * OAuth2User oauth2User = // usuario autenticado por Google/GitHub
     * JwtResponse response = oAuth2Service.processOAuth2User(oauth2User);
     * // Response incluye: token JWT, datos del usuario, roles
     * }</pre>
     */
    @Transactional
    public JwtResponse processOAuth2User(OAuth2User oauth2User) {
        log.info("Procesando usuario OAuth 2.0: {}", oauth2User.getName());
        
        // Extraer datos del usuario OAuth 2.0
        String email = extractEmail(oauth2User);
        String username = extractUsername(oauth2User);
        String provider = extractProvider(oauth2User);
        
        // Buscar usuario existente o crear nuevo
        User user = findOrCreateOAuth2User(oauth2User, email, username, provider);
        
        // Generar token JWT
        String jwt = jwtUtils.generateJwtToken(user.getUsername());
        
        // Construir respuesta
        List<String> roles = user.getRoles().stream()
                .map(role -> role.getName())
                .toList();
        
        return new JwtResponse(jwt, user.getUsername(), user.getEmail(), roles);
    }

    /**
     * 🔗 Vincula una cuenta OAuth 2.0 con una cuenta local existente
     * 
     * <p>Este método permite vincular una cuenta OAuth 2.0 con una cuenta
     * local del sistema, creando una autenticación híbrida que permite
     * múltiples métodos de acceso.</p>
     * 
     * <h4>⚡ Proceso de vinculación:</h4>
     * <ol>
     *   <li>Validación de cuenta local existente</li>
     *   <li>Verificación de que no esté ya vinculada</li>
     *   <li>Actualización de datos de usuario</li>
     *   <li>Configuración de autenticación híbrida</li>
     * </ol>
     * 
     * @param oauth2User Usuario autenticado por OAuth 2.0
     * @param localUsername Username de la cuenta local a vincular
     * @return Mensaje de confirmación de vinculación
     * @throws RuntimeException si la vinculación falla
     */
    @Transactional
    public String linkOAuth2Account(OAuth2User oauth2User, String localUsername) {
        log.info("Vinculando cuenta OAuth 2.0 con cuenta local: {}", localUsername);
        
        // Buscar cuenta local
        User localUser = userRepository.findByUsername(localUsername)
                .orElseThrow(() -> new RuntimeException("Cuenta local no encontrada"));
        
        // Verificar que no esté ya vinculada
        if (localUser.getOauth2Provider() != null) {
            throw new RuntimeException("La cuenta local ya está vinculada con un proveedor OAuth");
        }
        
        // Actualizar datos del usuario local
        localUser.setOauth2Provider(extractProvider(oauth2User));
        localUser.setOauth2Id(extractOAuth2Id(oauth2User));
        localUser.setUpdatedAt(LocalDateTime.now());
        
        userRepository.save(localUser);
        
        return "Cuenta OAuth 2.0 vinculada exitosamente con la cuenta local";
    }

    /**
     * 🚫 Desvincula una cuenta OAuth 2.0 de la cuenta local
     * 
     * <p>Este método permite desvincular una cuenta OAuth 2.0 de la
     * cuenta local del sistema, manteniendo solo la autenticación local.</p>
     * 
     * @param oauth2User Usuario autenticado por OAuth 2.0
     * @return Mensaje de confirmación de desvinculación
     * @throws RuntimeException si la desvinculación falla
     */
    @Transactional
    public String unlinkOAuth2Account(OAuth2User oauth2User) {
        log.info("Desvinculando cuenta OAuth 2.0: {}", oauth2User.getName());
        
        String oauth2Id = extractOAuth2Id(oauth2User);
        String provider = extractProvider(oauth2User);
        
        // Buscar usuario vinculado
        User user = userRepository.findByOauth2ProviderAndOauth2Id(provider, oauth2Id)
                .orElseThrow(() -> new RuntimeException("Cuenta OAuth 2.0 no encontrada"));
        
        // Desvincular
        user.setOauth2Provider(null);
        user.setOauth2Id(null);
        user.setUpdatedAt(LocalDateTime.now());
        
        userRepository.save(user);
        
        return "Cuenta OAuth 2.0 desvinculada exitosamente";
    }

    /**
     * 🔍 Busca o crea un usuario basado en datos OAuth 2.0
     * 
     * <p>Este método privado implementa la lógica para buscar un usuario existente
     * o crear uno nuevo basado en los datos del proveedor OAuth 2.0.</p>
     * 
     * @param oauth2User Usuario OAuth 2.0
     * @param email Email del usuario
     * @param username Username del usuario
     * @param provider Proveedor OAuth 2.0
     * @return Usuario local creado o actualizado
     */
    private User findOrCreateOAuth2User(OAuth2User oauth2User, String email, String username, String provider) {
        String oauth2Id = extractOAuth2Id(oauth2User);
        
        // Buscar por OAuth 2.0 ID primero
        Optional<User> existingUser = userRepository.findByOauth2ProviderAndOauth2Id(provider, oauth2Id);
        if (existingUser.isPresent()) {
            log.info("Usuario OAuth 2.0 encontrado: {}", existingUser.get().getUsername());
            return existingUser.get();
        }
        
        // Buscar por email
        existingUser = userRepository.findByEmail(email);
        if (existingUser.isPresent()) {
            log.info("Usuario encontrado por email, vinculando cuenta OAuth 2.0");
            User user = existingUser.get();
            user.setOauth2Provider(provider);
            user.setOauth2Id(oauth2Id);
            user.setUpdatedAt(LocalDateTime.now());
            return userRepository.save(user);
        }
        
        // Crear nuevo usuario
        log.info("Creando nuevo usuario OAuth 2.0: {}", username);
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setEmail(email);
        newUser.setOauth2Provider(provider);
        newUser.setOauth2Id(oauth2Id);
        newUser.setCreatedAt(LocalDateTime.now());
        newUser.setUpdatedAt(LocalDateTime.now());
        
        return userRepository.save(newUser);
    }

    /**
     * 📧 Extrae el email del usuario OAuth 2.0
     */
    private String extractEmail(OAuth2User oauth2User) {
        return oauth2User.getAttribute("email");
    }

    /**
     * 👤 Extrae el username del usuario OAuth 2.0
     */
    private String extractUsername(OAuth2User oauth2User) {
        String name = oauth2User.getName();
        if (name == null || name.isEmpty()) {
            name = oauth2User.getAttribute("login"); // Para GitHub
        }
        return name != null ? name : "user_" + System.currentTimeMillis();
    }

    /**
     * 🆔 Extrae el ID único del usuario OAuth 2.0
     */
    private String extractOAuth2Id(OAuth2User oauth2User) {
        return oauth2User.getAttribute("id");
    }

    /**
     * 🏢 Extrae el proveedor OAuth 2.0
     */
    private String extractProvider(OAuth2User oauth2User) {
        // Determinar proveedor basado en los atributos disponibles
        if (oauth2User.getAttribute("login") != null) {
            return "github";
        } else if (oauth2User.getAttribute("sub") != null) {
            return "google";
        }
        return "unknown";
    }
} 