package com.ejemplo.authjwt.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/api/public")
    public ResponseEntity<String> publicEndpoint() {
        return ResponseEntity.ok("Este endpoint es público");
    }

    @GetMapping("/api/user")
    public ResponseEntity<String> userEndpoint() {
        return ResponseEntity.ok("Acceso permitido a usuarios autenticados");
    }

    @GetMapping("/api/admin")
    public ResponseEntity<String> adminEndpoint() {
        return ResponseEntity.ok("Solo ADMIN puede ver esto");
    }
}
