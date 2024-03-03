package br.com.wex.transaction.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring boot configuration class for swagger documentation.
 * @since 1.0.0
 */
@Configuration
@SuppressWarnings("MissingJavadocMethod")
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI(final SpringdocConfig springdocConfig) {

        return new OpenAPI().info(new Info().title(springdocConfig.getTitle())
            .version(springdocConfig.getVersion())
            .description(springdocConfig.getDescription())
            .termsOfService(springdocConfig.getTermsOfService())
            .contact(new Contact().name("Janayna Fernandes").email("fernandesmjanayna@gmail.com")));
    }

    @Data
    @Configuration
    @ConfigurationProperties(prefix = "springdoc.info")
    public static class SpringdocConfig {

        @Value("${version:app.version}")
        private String version;
        @Value("${title:app.name}")
        private String title;
        private String description;
        private String termsOfService;

    }
}
