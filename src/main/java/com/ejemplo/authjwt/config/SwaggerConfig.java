package com.ejemplo.authjwt.config;

import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("API de Autenticación con JWT")
                .version("1.0")
                .description("Proyecto para demostrar autenticación y autorización con Spring Boot y JWT"));
    }
}
