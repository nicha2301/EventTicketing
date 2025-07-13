package com.eventticketing.backend.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.servers.Server
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class OpenApiConfig {

    @Value("\${app.url}")
    private lateinit var appUrl: String
    
    @Value("\${app.cors.allowed-origins}")
    private lateinit var allowedOrigins: String

    @Bean
    fun openAPI(): OpenAPI {
        return OpenAPI()
            .addServersItem(Server().url(appUrl))
            .info(
                Info()
                    .title("Event Ticketing API")
                    .description("API documentation for Event Ticketing System")
                    .version("v1.0")
                    .contact(
                        Contact()
                            .name("Event Ticketing Team")
                            .email("support@eventticketing.com")
                    )
                    .license(
                        License()
                            .name("Private License")
                    )
            )
            .addSecurityItem(SecurityRequirement().addList("bearerAuth"))
            .components(
                Components()
                    .addSecuritySchemes(
                        "bearerAuth",
                        SecurityScheme()
                            .name("bearerAuth")
                            .type(SecurityScheme.Type.HTTP)
                            .scheme("bearer")
                            .bearerFormat("JWT")
                    )
            )
    }
    
    @Bean
    fun corsConfigurer(): WebMvcConfigurer {
        return object : WebMvcConfigurer {
            override fun addCorsMappings(registry: CorsRegistry) {
                registry.addMapping("/v3/api-docs/**")
                    .allowedOrigins(*allowedOrigins.split(",").map { it.trim() }.toTypedArray())
                    .allowedMethods("GET")
                
                registry.addMapping("/swagger-ui/**")
                    .allowedOrigins(*allowedOrigins.split(",").map { it.trim() }.toTypedArray())
                    .allowedMethods("GET")
            }
        }
    }
} 