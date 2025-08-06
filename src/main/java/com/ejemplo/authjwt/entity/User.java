package com.ejemplo.authjwt.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * 👤 Entidad Usuario con Soporte para Autenticación Híbrida
 * 
 * <p>Esta entidad representa un usuario en el sistema con soporte para
 * múltiples métodos de autenticación: local (username/password) y OAuth 2.0.</p>
 * 
 * <h3>🎯 Métodos de autenticación soportados:</h3>
 * <ul>
 *   <li>✅ Autenticación local con username/password</li>
 *   <li>✅ Autenticación OAuth 2.0 (Google, GitHub)</li>
 *   <li>✅ Autenticación híbrida (vincular cuentas)</li>
 * </ul>
 * 
 * <h3>🔐 Características de seguridad:</h3>
 * <ul>
 *   <li>Contraseñas encriptadas con BCrypt</li>
 *   <li>Campos únicos para prevenir duplicados</li>
 *   <li>Timestamps automáticos de auditoría</li>
 *   <li>Relación muchos a muchos con roles</li>
 * </ul>
 * 
 * @author Alejandro Arango
 * @version 1.0
 * @since 2025-01-01
 * @see Role
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = true) // Nullable para usuarios OAuth 2.0
    private String password;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Campos para OAuth 2.0
    @Column(name = "oauth2_provider")
    private String oauth2Provider; // "google", "github", etc.

    @Column(name = "oauth2_id")
    private String oauth2Id; // ID único del proveedor OAuth 2.0

    @Column(name = "oauth2_name")
    private String oauth2Name; // Nombre completo del proveedor OAuth 2.0

    @Column(name = "oauth2_picture")
    private String oauth2Picture; // URL de la foto de perfil

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
