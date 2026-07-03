package com.techvedika.harmonycvi.gateway.util;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@SecurityScheme(
    name = "jwtAuth",
    type = SecuritySchemeType.APIKEY,
    in = SecuritySchemeIn.HEADER,
    paramName = "Authorization"
)
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI(
            @Value("${server.gateway-url}") String apiServerUrl
    ) {
        return new OpenAPI()
                .info(new io.swagger.v3.oas.models.info.Info()
                        .title("HarmonyCVI API")
                        .version("v1"))
                .addSecurityItem(new io.swagger.v3.oas.models.security.SecurityRequirement().addList("jwtAuth"))
                .servers(List.of(new Server().url(apiServerUrl)));
    }
}
