package com.ejemplo.authjwt.controller;

import com.ejemplo.authjwt.dto.JwtResponse;
import com.ejemplo.authjwt.service.OAuth2Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

/**
 * 🔐 Controlador para Autenticación OAuth 2.0
 * 
 * <p>Este controlador maneja la autenticación mediante proveedores OAuth 2.0
 * como Google y GitHub, implementando flujos de autorización seguros y
 * generación de tokens JWT personalizados.</p>
 * 
 * <h3>🎯 Proveedores soportados:</h3>
 * <ul>
 *   <li>✅ Google OAuth 2.0</li>
 *   <li>✅ GitHub OAuth 2.0</li>
 *   <li>✅ Expansión futura a otros proveedores</li>
 * </ul>
 * 
 * <h3>🔒 Características de seguridad:</h3>
 * <ul>
 *   <li>Validación de tokens OAuth 2.0</li>
 *   <li>Generación de JWT personalizados</li>
 *   <li>Mapeo seguro de usuarios OAuth</li>
 *   <li>Gestión de sesiones híbridas</li>
 * </ul>
 * 
 * @author Alejandro Arango
 * @version 1.0
 * @since 2025-01-01
 * @see OAuth2Service
 * @see JwtResponse
 */
@Slf4j
@RestController
@RequestMapping("/api/oauth2")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class OAuth2Controller {

    private final OAuth2Service oAuth2Service;

    /**
     * 🔑 Endpoint de callback para autenticación OAuth 2.0
     * 
     * <p>Este endpoint recibe la respuesta del proveedor OAuth 2.0 después
     * de la autenticación exitosa y genera un token JWT personalizado.</p>
     * 
     * <h4>⚡ Flujo de autenticación OAuth 2.0:</h4>
     * <ol>
     *   <li>Usuario redirigido al proveedor (Google/GitHub)</li>
     *   <li>Autenticación exitosa en el proveedor</li>
     *   <li>Callback con código de autorización</li>
     *   <li>Intercambio de código por token de acceso</li>
     *   <li>Obtención de información del usuario</li>
     *   <li>Generación de JWT personalizado</li>
     * </ol>
     * 
     * <h4>🔐 Información obtenida del proveedor:</h4>
     * <ul>
     *   <li><strong>Google:</strong> email, nombre, foto de perfil</li>
     *   <li><strong>GitHub:</strong> username, email, repositorios públicos</li>
     *   <li><strong>Común:</strong> ID único, información de perfil</li>
     * </ul>
     * 
     * @param oauth2User Usuario autenticado por OAuth 2.0
     * @return ResponseEntity con token JWT y datos del usuario
     * 
     * @example
     * <pre>{@code
     * // Flujo de autenticación:
     * // 1. GET /oauth2/authorization/google
     * // 2. Usuario autenticado en Google
     * // 3. Callback a este endpoint
     * // 4. Respuesta con JWT personalizado
     * }</pre>
     */
    @GetMapping("/callback")
    public ResponseEntity<JwtResponse> oauth2Callback(@AuthenticationPrincipal OAuth2User oauth2User) {
        log.info("OAuth 2.0 callback recibido para usuario: {}", oauth2User.getName());
        
        JwtResponse jwtResponse = oAuth2Service.processOAuth2User(oauth2User);
        
        return ResponseEntity.ok(jwtResponse);
    }

    /**
     * 🔍 Endpoint para obtener información del usuario OAuth 2.0
     * 
     * <p>Este endpoint permite obtener información detallada del usuario
     * autenticado mediante OAuth 2.0, incluyendo datos del proveedor.</p>
     * 
     * @param oauth2User Usuario autenticado por OAuth 2.0
     * @return ResponseEntity con información detallada del usuario
     */
    @GetMapping("/user-info")
    public ResponseEntity<OAuth2User> getUserInfo(@AuthenticationPrincipal OAuth2User oauth2User) {
        log.info("Solicitud de información de usuario OAuth 2.0: {}", oauth2User.getName());
        return ResponseEntity.ok(oauth2User);
    }

    /**
     * 🔗 Endpoint para vincular cuenta OAuth 2.0 con cuenta local
     * 
     * <p>Este endpoint permite vincular una cuenta OAuth 2.0 existente
     * con una cuenta local del sistema, creando una autenticación híbrida.</p>
     * 
     * @param oauth2User Usuario autenticado por OAuth 2.0
     * @param localUsername Username de la cuenta local a vincular
     * @return ResponseEntity con confirmación de vinculación
     */
    @PostMapping("/link-account")
    public ResponseEntity<String> linkOAuth2Account(
            @AuthenticationPrincipal OAuth2User oauth2User,
            @RequestParam String localUsername) {
        
        log.info("Vinculando cuenta OAuth 2.0 {} con cuenta local: {}", 
                oauth2User.getName(), localUsername);
        
        String result = oAuth2Service.linkOAuth2Account(oauth2User, localUsername);
        return ResponseEntity.ok(result);
    }

    /**
     * 🚫 Endpoint para desvincular cuenta OAuth 2.0
     * 
     * <p>Este endpoint permite desvincular una cuenta OAuth 2.0
     * de la cuenta local del sistema.</p>
     * 
     * @param oauth2User Usuario autenticado por OAuth 2.0
     * @return ResponseEntity con confirmación de desvinculación
     */
    @DeleteMapping("/unlink-account")
    public ResponseEntity<String> unlinkOAuth2Account(@AuthenticationPrincipal OAuth2User oauth2User) {
        log.info("Desvinculando cuenta OAuth 2.0: {}", oauth2User.getName());
        
        String result = oAuth2Service.unlinkOAuth2Account(oauth2User);
        return ResponseEntity.ok(result);
    }
} 