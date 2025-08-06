package com.ejemplo.authjwt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.ejemplo.authjwt")
public class AuthJwtApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthJwtApplication.class, args);
    }

}