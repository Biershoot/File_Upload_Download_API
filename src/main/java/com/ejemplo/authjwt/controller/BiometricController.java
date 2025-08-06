package com.ejemplo.authjwt.controller;

import com.ejemplo.authjwt.dto.JwtResponse;
import com.ejemplo.authjwt.service.BiometricService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 🔐 Controlador para Autenticación Biométrica
 * 
 * <p>Este controlador maneja la autenticación biométrica utilizando tecnologías
 * como huellas dactilares, reconocimiento facial y autenticación por voz.</p>
 * 
 * <h3>🎯 Tecnologías biométricas soportadas:</h3>
 * <ul>
 *   <li>✅ Huellas dactilares (Fingerprint)</li>
 *   <li>✅ Reconocimiento facial (Face Recognition)</li>
 *   <li>✅ Autenticación por voz (Voice Recognition)</li>
 *   <li>✅ Escaneo de iris (Iris Scanning)</li>
 * </ul>
 * 
 * <h3>🔒 Características de seguridad:</h3>
 * <ul>
 *   <li>Encriptación de datos biométricos</li>
 *   <li>Validación de liveness detection</li>
 *   <li>Prevención de ataques de spoofing</li>
 *   <li>Gestión segura de templates biométricos</li>
 * </ul>
 * 
 * @author Alejandro Arango
 * @version 1.0
 * @since 2025-01-01
 * @see BiometricService
 * @see JwtResponse
 */
@Slf4j
@RestController
@RequestMapping("/api/biometric")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class BiometricController {

    private final BiometricService biometricService;

    /**
     * 🔐 Endpoint para registro de datos biométricos
     * 
     * <p>Este endpoint permite registrar datos biométricos de un usuario
     * para futuras autenticaciones biométricas.</p>
     * 
     * <h4>⚡ Proceso de registro:</h4>
     * <ol>
     *   <li>Validación de usuario autenticado</li>
     *   <li>Captura de datos biométricos</li>
     *   <li>Procesamiento y encriptación</li>
     *   <li>Almacenamiento seguro de templates</li>
     *   <li>Confirmación de registro exitoso</li>
     * </ol>
     * 
     * <h4>🛡️ Medidas de seguridad:</h4>
     * <ul>
     *   <li>Encriptación AES-256 de datos biométricos</li>
     *   <li>Validación de calidad de datos</li>
     *   <li>Prevención de duplicados</li>
     *   <li>Auditoría de registros</li>
     * </ul>
     * 
     * @param biometricData Datos biométricos del usuario
     * @param biometricType Tipo de biometría (fingerprint, face, voice, iris)
     * @return ResponseEntity con confirmación de registro
     * 
     * @example
     * <pre>{@code
     * POST /api/biometric/register
     * Content-Type: application/json
     * 
     * {
     *   "biometricData": "base64EncodedBiometricData",
     *   "biometricType": "fingerprint",
     *   "userId": "user123"
     * }
     * }</pre>
     */
    @PostMapping("/register")
    public ResponseEntity<String> registerBiometric(
            @RequestBody String biometricData,
            @RequestParam String biometricType) {
        
        log.info("Registrando datos biométricos de tipo: {}", biometricType);
        
        String result = biometricService.registerBiometric(biometricData, biometricType);
        return ResponseEntity.ok(result);
    }

    /**
     * 🔑 Endpoint para autenticación biométrica
     * 
     * <p>Este endpoint permite autenticar usuarios mediante datos biométricos
     * previamente registrados en el sistema.</p>
     * 
     * <h4>⚡ Proceso de autenticación:</h4>
     * <ol>
     *   <li>Captura de datos biométricos</li>
     *   <li>Comparación con templates almacenados</li>
     *   <li>Validación de liveness detection</li>
     *   <li>Generación de token JWT</li>
     *   <li>Retorno de respuesta autenticada</li>
     * </ol>
     * 
     * <h4>🔐 Validaciones implementadas:</h4>
     * <ul>
     *   <li>Comparación de templates biométricos</li>
     *   <li>Detección de ataques de spoofing</li>
     *   <li>Validación de calidad de datos</li>
     *   <li>Verificación de usuario existente</li>
     * </ul>
     * 
     * @param biometricData Datos biométricos para autenticación
     * @param biometricType Tipo de biometría utilizada
     * @return ResponseEntity con token JWT y datos del usuario
     * 
     * @example
     * <pre>{@code
     * POST /api/biometric/authenticate
     * Content-Type: application/json
     * 
     * {
     *   "biometricData": "base64EncodedBiometricData",
     *   "biometricType": "fingerprint"
     * }
     * 
     * Response:
     * {
     *   "token": "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9...",
     *   "username": "user123",
     *   "email": "user@example.com",
     *   "roles": ["ROLE_USER"]
     * }
     * }</pre>
     */
    @PostMapping("/authenticate")
    public ResponseEntity<JwtResponse> authenticateBiometric(
            @RequestBody String biometricData,
            @RequestParam String biometricType) {
        
        log.info("Autenticación biométrica de tipo: {}", biometricType);
        
        JwtResponse response = biometricService.authenticateBiometric(biometricData, biometricType);
        return ResponseEntity.ok(response);
    }

    /**
     * 🚫 Endpoint para eliminar datos biométricos
     * 
     * <p>Este endpoint permite eliminar datos biométricos registrados
     * de un usuario específico.</p>
     * 
     * @param biometricType Tipo de biometría a eliminar
     * @return ResponseEntity con confirmación de eliminación
     */
    @DeleteMapping("/remove")
    public ResponseEntity<String> removeBiometric(@RequestParam String biometricType) {
        log.info("Eliminando datos biométricos de tipo: {}", biometricType);
        
        String result = biometricService.removeBiometric(biometricType);
        return ResponseEntity.ok(result);
    }

    /**
     * 📊 Endpoint para obtener estadísticas biométricas
     * 
     * <p>Este endpoint proporciona estadísticas sobre el uso de
     * autenticación biométrica en el sistema.</p>
     * 
     * @return ResponseEntity con estadísticas biométricas
     */
    @GetMapping("/stats")
    public ResponseEntity<String> getBiometricStats() {
        log.info("Solicitando estadísticas biométricas");
        
        String stats = biometricService.getBiometricStats();
        return ResponseEntity.ok(stats);
    }
} 