package com.example.healthtrackr.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@OpenAPIDefinition(
        info = @io.swagger.v3.oas.annotations.info.Info(
                title = "HealthTrackr API",
                version = "1.0.0",
                description = ""
        ),
        servers = {
                @Server(url = "http://localhost:8080", description = "Local Development Server"),
        },
        security = {
                @SecurityRequirement(name = "bearer-key")
        }
)
@Configuration
public class SwaggerConfig {


}
