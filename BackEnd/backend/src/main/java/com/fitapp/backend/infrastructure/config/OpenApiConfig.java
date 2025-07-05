package com.fitapp.backend.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${app.openapi.dev-url}")
    private String devUrl;

    @Bean
    public OpenAPI customOpenAPI() {
        Server devServer = new Server();
        devServer.setUrl(devUrl);
        devServer.setDescription("URL de desarrollo");

        Contact contact = new Contact();
        contact.setEmail("soporte@fitapp.com");
        contact.setName("Equipo FitApp");
        contact.setUrl("https://www.fitapp.com");

        License mitLicense = new License()
                .name("Licencia MIT")
                .url("https://opensource.org/licenses/MIT");

        Info info = new Info()
                .title("API de FitApp")
                .version("1.0.0")
                .contact(contact)
                .description("API para el sistema de gestión de rutinas deportivas FitApp")
                .license(mitLicense);

        return new OpenAPI()
                .info(info)
                .servers(List.of(devServer));
    }
}