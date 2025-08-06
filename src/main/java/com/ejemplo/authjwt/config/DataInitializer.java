package com.ejemplo.authjwt.config;

import com.ejemplo.authjwt.entity.Role;
import com.ejemplo.authjwt.entity.User;
import com.ejemplo.authjwt.repository.RoleRepository;
import com.ejemplo.authjwt.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("🚀 Inicializando base de datos...");

        // Inicializar roles si no existen
        Role userRole = null;
        Role adminRole = null;

        if (roleRepository.findByName(Role.RoleName.ROLE_USER).isEmpty()) {
            userRole = new Role();
            userRole.setName(Role.RoleName.ROLE_USER);
            userRole = roleRepository.save(userRole);
            System.out.println("✅ Rol USER creado");
        } else {
            userRole = roleRepository.findByName(Role.RoleName.ROLE_USER).get();
        }

        if (roleRepository.findByName(Role.RoleName.ROLE_ADMIN).isEmpty()) {
            adminRole = new Role();
            adminRole.setName(Role.RoleName.ROLE_ADMIN);
            adminRole = roleRepository.save(adminRole);
            System.out.println("✅ Rol ADMIN creado");
        } else {
            adminRole = roleRepository.findByName(Role.RoleName.ROLE_ADMIN).get();
        }

        // Crear usuario administrador por defecto si no existe
        if (!userRepository.existsByUsername("admin")) {
            User adminUser = new User();
            adminUser.setUsername("admin");
            adminUser.setEmail("admin@example.com");
            adminUser.setPassword(passwordEncoder.encode("admin123"));
            adminUser.setRoles(Set.of(adminRole, userRole));
            adminUser.setCreatedAt(LocalDateTime.now());
            adminUser.setUpdatedAt(LocalDateTime.now());

            userRepository.save(adminUser);
            System.out.println("✅ Usuario administrador creado:");
            System.out.println("   Username: admin");
            System.out.println("   Password: admin123");
            System.out.println("   Email: admin@example.com");
        }

        // Crear usuario de prueba por defecto
        if (!userRepository.existsByUsername("testuser")) {
            User testUser = new User();
            testUser.setUsername("testuser");
            testUser.setEmail("test@example.com");
            testUser.setPassword(passwordEncoder.encode("password123"));
            testUser.setRoles(Set.of(userRole));
            testUser.setCreatedAt(LocalDateTime.now());
            testUser.setUpdatedAt(LocalDateTime.now());

            userRepository.save(testUser);
            System.out.println("✅ Usuario de prueba creado:");
            System.out.println("   Username: testuser");
            System.out.println("   Password: password123");
            System.out.println("   Email: test@example.com");
        }

        System.out.println("🎯 Base de datos inicializada correctamente!");
        System.out.println("📊 Consola H2 disponible en: http://localhost:8080/h2-console");
        System.out.println("   JDBC URL: jdbc:h2:mem:testdb");
        System.out.println("   Username: root");
        System.out.println("   Password: root");
    }
}
