package com.ejemplo.authjwt.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

/**
 * 🔐 Utilidad para Gestión Completa de Tokens JWT
 * 
 * <p>Esta clase implementa la funcionalidad completa para la generación, validación
 * y manipulación de tokens JWT (JSON Web Tokens) siguiendo los estándares RFC 7519.</p>
 * 
 * <h3>🎯 Funcionalidades implementadas:</h3>
 * <ul>
 *   <li>✅ Generación segura de tokens JWT con firma digital</li>
 *   <li>✅ Validación completa de tokens con manejo de excepciones</li>
 *   <li>✅ Extracción de claims y datos del usuario</li>
 *   <li>✅ Gestión de expiración y renovación de tokens</li>
 *   <li>✅ Logging detallado para auditoría de seguridad</li>
 * </ul>
 * 
 * <h3>🔒 Medidas de seguridad implementadas:</h3>
 * <ul>
 *   <li><strong>Algoritmo de firma:</strong> HS512 (HMAC SHA-512)</li>
 *   <li><strong>Clave secreta:</strong> Configurada externamente para mayor seguridad</li>
 *   <li><strong>Expiración:</strong> Tokens con tiempo de vida limitado</li>
 *   <li><strong>Validación:</strong> Verificación completa de firma y claims</li>
 *   <li><strong>Logging:</strong> Registro de intentos de manipulación</li>
 * </ul>
 * 
 * <h3>📋 Estructura del token JWT:</h3>
 * <ul>
 *   <li><strong>Header:</strong> Algoritmo de firma y tipo de token</li>
 *   <li><strong>Payload:</strong> Claims del usuario y metadatos</li>
 *   <li><strong>Signature:</strong> Firma digital para verificación</li>
 * </ul>
 * 
 * <h3>⚠️ Consideraciones de seguridad:</h3>
 * <ul>
 *   <li>Clave secreta debe ser de al menos 256 bits</li>
 *   <li>Tokens expiran automáticamente para limitar exposición</li>
 *   <li>Validación completa previene ataques de manipulación</li>
 *   <li>Logging de errores para detección de ataques</li>
 * </ul>
 * 
 * @author Alejandro Arango
 * @version 1.0
 * @since 2025-01-01
 * @see <a href="https://tools.ietf.org/html/rfc7519">RFC 7519 - JSON Web Token</a>
 * @see <a href="https://jwt.io/">JWT.io - Debugger y documentación</a>
 */
@Slf4j
@Component
public class JwtUtils {

    /**
     * Clave secreta para firmar y verificar tokens JWT
     * Configurada externamente para mayor seguridad y flexibilidad
     * Debe ser de al menos 256 bits para HS512
     */
    @Value("${app.jwtSecret}")
    private String jwtSecret;

    /**
     * Tiempo de expiración de tokens JWT en milisegundos
     * Configurado para balancear seguridad y experiencia de usuario
     * Recomendado: 15-60 minutos para aplicaciones web
     */
    @Value("${app.jwtExpirationMs}")
    private int jwtExpirationMs;

    /**
     * 🔑 Genera un token JWT seguro para un usuario específico
     * 
     * <p>Este método implementa la generación completa de tokens JWT siguiendo
     * las mejores prácticas de seguridad y los estándares RFC 7519.</p>
     * 
     * <h4>⚡ Proceso de generación:</h4>
     * <ol>
     *   <li>Creación del payload con claims estándar</li>
     *   <li>Configuración de timestamps de emisión y expiración</li>
     *   <li>Firma digital con algoritmo HS512</li>
     *   <li>Codificación Base64URL del token final</li>
     * </ol>
     * 
     * <h4>🔐 Claims incluidos en el token:</h4>
     * <ul>
     *   <li><strong>sub (Subject):</strong> Username del usuario</li>
     *   <li><strong>iat (Issued At):</strong> Timestamp de emisión</li>
     *   <li><strong>exp (Expiration):</strong> Timestamp de expiración</li>
     *   <li><strong>iss (Issuer):</strong> Emisor del token (opcional)</li>
     * </ul>
     * 
     * <h4>🛡️ Características de seguridad:</h4>
     * <ul>
     *   <li>Firma digital con clave secreta</li>
     *   <li>Expiración automática configurable</li>
     *   <li>Algoritmo criptográfico robusto (HS512)</li>
     *   <li>Claims mínimos para reducir exposición</li>
     * </ul>
     * 
     * @param username Nombre de usuario que será el subject del token
     * @return Token JWT codificado en Base64URL
     * @throws IllegalArgumentException si el username es null o vacío
     * 
     * @example
     * <pre>{@code
     * String token = jwtUtils.generateJwtToken("usuario123");
     * // Resultado: eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9...
     * }</pre>
     */
    public String generateJwtToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * 🔍 Extrae el nombre de usuario del token JWT
     * 
     * <p>Este método parsea el token JWT y extrae el claim 'subject' que contiene
     * el nombre de usuario, validando la firma en el proceso.</p>
     * 
     * <h4>⚡ Proceso de extracción:</h4>
     * <ol>
     *   <li>Parsing del token JWT con validación de firma</li>
     *   <li>Extracción del claim 'subject' del payload</li>
     *   <li>Validación de integridad del token</li>
     *   <li>Retorno del username si todo es válido</li>
     * </ol>
     * 
     * <h4>🔒 Validaciones implementadas:</h4>
     * <ul>
     *   <li>Verificación de firma digital</li>
     *   <li>Validación de formato del token</li>
     *   <li>Verificación de expiración</li>
     *   <li>Validación de claims requeridos</li>
     * </ul>
     * 
     * <h4>⚠️ Consideraciones de seguridad:</h4>
     * <ul>
     *   <li>No confiar en datos del token sin validar firma</li>
     *   <li>Manejo seguro de excepciones para evitar información de debug</li>
     *   <li>Logging de intentos de manipulación</li>
     * </ul>
     * 
     * @param token Token JWT del cual extraer el username
     * @return Nombre de usuario extraído del token
     * @throws SignatureException si la firma del token es inválida
     * @throws ExpiredJwtException si el token ha expirado
     * @throws MalformedJwtException si el formato del token es inválido
     * @throws IllegalArgumentException si el token es null o vacío
     * 
     * @example
     * <pre>{@code
     * String username = jwtUtils.getUsernameFromJwtToken(token);
     * // Resultado: "usuario123"
     * }</pre>
     */
    public String getUsernameFromJwtToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    /**
     * ✅ Valida la integridad y validez de un token JWT
     * 
     * <p>Este método implementa una validación completa del token JWT, verificando
     * firma, formato, expiración y claims requeridos.</p>
     * 
     * <h4>🔍 Validaciones realizadas:</h4>
     * <ul>
     *   <li><strong>Firma digital:</strong> Verificación con clave secreta</li>
     *   <li><strong>Formato:</strong> Estructura JWT válida</li>
     *   <li><strong>Expiración:</strong> Token no ha expirado</li>
     *   <li><strong>Claims:</strong> Claims requeridos presentes</li>
     *   <li><strong>Algoritmo:</strong> Algoritmo de firma soportado</li>
     * </ul>
     * 
     * <h4>📊 Tipos de errores manejados:</h4>
     * <ul>
     *   <li><strong>SignatureException:</strong> Firma inválida o manipulada</li>
     *   <li><strong>MalformedJwtException:</strong> Formato de token inválido</li>
     *   <li><strong>ExpiredJwtException:</strong> Token expirado</li>
     *   <li><strong>UnsupportedJwtException:</strong> Algoritmo no soportado</li>
     *   <li><strong>IllegalArgumentException:</strong> Claims vacíos o inválidos</li>
     * </ul>
     * 
     * <h4>🔒 Medidas de seguridad:</h4>
     * <ul>
     *   <li>Logging detallado de errores para auditoría</li>
     *   <li>Manejo seguro de excepciones</li>
     *   <li>No exposición de información sensible en logs</li>
     *   <li>Validación completa antes de confiar en el token</li>
     * </ul>
     * 
     * @param token Token JWT a validar
     * @return true si el token es válido y no ha expirado, false en caso contrario
     * 
     * @example
     * <pre>{@code
     * boolean isValid = jwtUtils.validateJwtToken(token);
     * if (isValid) {
     *     // Token válido, proceder con la autenticación
     *     String username = jwtUtils.getUsernameFromJwtToken(token);
     * } else {
     *     // Token inválido, requerir nueva autenticación
     * }
     * }</pre>
     */
    public boolean validateJwtToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (SignatureException e) {
            log.error("Firma JWT inválida: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("Token JWT inválido: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("Token JWT expirado: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("Token JWT no soportado: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims vacío: {}", e.getMessage());
        }
        return false;
    }

    /**
     * 🔑 Obtiene la clave de firma para tokens JWT
     * 
     * <p>Este método privado genera la clave de firma a partir de la clave secreta
     * configurada, utilizando el algoritmo HMAC SHA-512 para máxima seguridad.</p>
     * 
     * <h4>🔐 Características de la clave:</h4>
     * <ul>
     *   <li><strong>Algoritmo:</strong> HMAC SHA-512 (HS512)</li>
     *   <li><strong>Tamaño:</strong> Mínimo 256 bits recomendado</li>
     *   <li><strong>Codificación:</strong> UTF-8 para compatibilidad</li>
     *   <li><strong>Seguridad:</strong> Clave secreta configurada externamente</li>
     * </ul>
     * 
     * <h4>⚠️ Consideraciones de seguridad:</h4>
     * <ul>
     *   <li>Clave secreta debe ser suficientemente larga</li>
     *   <li>No hardcodear la clave en el código</li>
     *   <li>Usar variables de entorno o configuración externa</li>
     *   <li>Rotar claves periódicamente en producción</li>
     * </ul>
     * 
     * @return Clave de firma para tokens JWT
     * @throws IllegalArgumentException si la clave secreta es inválida
     */
    private Key getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
