package br.com.wex.transaction.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Spring boot configuration class for rest template.
 * @since 1.0.0
 */
@Configuration
@SuppressWarnings("MissingJavadocMethod")
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
