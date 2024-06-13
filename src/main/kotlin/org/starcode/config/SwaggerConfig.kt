package org.starcode.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfig {

    @Bean
    fun customSwagger(): OpenAPI{
        return OpenAPI()
            .info(
                Info()
                    .title("RESTFul API with Kotlin and Spring Boot 3")
                    .version("v1")
                    .description("Faster API")
                    .license(
                        License().name("Apache 2.0")
                            .url("https://github.com/kaioa1meida/restfull-spring-kotlin/blob/master/LICENSE")
                    )
            )
    }
}