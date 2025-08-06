package com.ejemplo.authjwt.repository;

import com.ejemplo.authjwt.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 📚 Repositorio para Gestión de Usuarios con Soporte OAuth 2.0
 * 
 * <p>Este repositorio proporciona métodos de acceso a datos para la entidad User,
 * incluyendo consultas específicas para usuarios OAuth 2.0 y autenticación híbrida.</p>
 * 
 * <h3>🎯 Funcionalidades implementadas:</h3>
 * <ul>
 *   <li>✅ Consultas básicas de usuarios (CRUD)</li>
 *   <li>✅ Búsqueda por username y email</li>
 *   <li>✅ Validación de existencia de usuarios</li>
 *   <li>✅ Consultas específicas para OAuth 2.0</li>
 *   <li>✅ Búsqueda por proveedor OAuth 2.0</li>
 * </ul>
 * 
 * <h3>🔍 Consultas OAuth 2.0 disponibles:</h3>
 * <ul>
 *   <li><strong>findByOauth2ProviderAndOauth2Id:</strong> Buscar por proveedor e ID</li>
 *   <li><strong>findByOauth2Provider:</strong> Buscar por proveedor</li>
 *   <li><strong>findByOauth2Id:</strong> Buscar por ID OAuth 2.0</li>
 *   <li><strong>findByOauth2ProviderIsNotNull:</strong> Usuarios OAuth 2.0</li>
 * </ul>
 * 
 * @author Alejandro Arango
 * @version 1.0
 * @since 2025-01-01
 * @see User
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // Consultas básicas
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    
    // Consultas OAuth 2.0
    Optional<User> findByOauth2ProviderAndOauth2Id(String provider, String oauth2Id);
    List<User> findByOauth2Provider(String provider);
    Optional<User> findByOauth2Id(String oauth2Id);
    List<User> findByOauth2ProviderIsNotNull();
    
    // Consultas híbridas
    Optional<User> findByUsernameOrEmail(String username, String email);
    boolean existsByUsernameOrEmail(String username, String email);
}