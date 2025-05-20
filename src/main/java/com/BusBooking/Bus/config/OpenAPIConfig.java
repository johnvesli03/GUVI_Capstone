package com.BusBooking.Bus.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {

    // ✅ Show all endpoints in Swagger (including /admin/**, /user/**)
    @Bean
    public GroupedOpenApi fullApi() {
        return GroupedOpenApi.builder()
                .group("full-api")
                .pathsToMatch("/**")
                .build();
    }

    // ✅ Optional security scheme (not enforced)
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("Bus Booking API").version("1.0").description("Full API documentation"))
                .components(new Components()
                        .addSecuritySchemes("basicAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("basic")));
    }
}
