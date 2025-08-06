package com.ejemplo.authjwt.config;

import com.ejemplo.authjwt.entity.Role;
import com.ejemplo.authjwt.entity.User;
import com.ejemplo.authjwt.repository.RoleRepository;
import com.ejemplo.authjwt.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.boot.CommandLineRunner;

import java.time.LocalDateTime;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Inicializar roles básicos
        initializeRoles();
        // Inicializar usuarios por defecto
        initializeDefaultUsers();

        printDatabaseInfo();
    }

    private void initializeRoles() {
        if (roleRepository.findByName("ROLE_USER").isEmpty()) {
            roleRepository.save(new Role(null, "ROLE_USER"));
            System.out.println("✅ Rol USER creado");
        }

        if (roleRepository.findByName("ROLE_ADMIN").isEmpty()) {
            roleRepository.save(new Role(null, "ROLE_ADMIN"));
            System.out.println("✅ Rol ADMIN creado");
        }
    }

    private void initializeDefaultUsers() {
        // Crear usuario admin si no existe
        if (!userRepository.existsByUsername("admin")) {
            createAdminUser();
        }

        // Crear usuario de prueba si no existe
        if (!userRepository.existsByUsername("testuser")) {
            createTestUser();
        }
    }

    private void createAdminUser() {
        User adminUser = new User();
        adminUser.setUsername("admin");
        adminUser.setEmail("admin@example.com");
        adminUser.setPassword(passwordEncoder.encode("admin123"));
        adminUser.setRoles(Set.of(
            roleRepository.findByName("ROLE_ADMIN").get(),
            roleRepository.findByName("ROLE_USER").get()
        ));
        adminUser.setCreatedAt(LocalDateTime.now());
        adminUser.setUpdatedAt(LocalDateTime.now());

        userRepository.save(adminUser);
        System.out.println("✅ Usuario administrador creado");
    }

    private void createTestUser() {
        User testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setRoles(Set.of(roleRepository.findByName("ROLE_USER").get()));
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());

        userRepository.save(testUser);
        System.out.println("✅ Usuario de prueba creado");
    }

    private void printDatabaseInfo() {
        System.out.println("\n🎯 Base de datos inicializada correctamente!");
        System.out.println("\n📝 Usuarios por defecto creados:");
        System.out.println("   1. Admin");
        System.out.println("      - Username: admin");
        System.out.println("      - Password: admin123");
        System.out.println("      - Roles: ROLE_ADMIN, ROLE_USER");
        System.out.println("\n   2. Usuario de prueba");
        System.out.println("      - Username: testuser");
        System.out.println("      - Password: password123");
        System.out.println("      - Roles: ROLE_USER");
        System.out.println("\n🔐 Prueba los endpoints en: http://localhost:8086/swagger-ui/index.html");
    }
}
