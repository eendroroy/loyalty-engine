package io.github.eendroroy.loyalty.config;

import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.DeserializationFeature;

@Configuration
public class JacksonConfig {

    @Bean
    public JsonMapperBuilderCustomizer jacksonCustomizer() {
        // Jackson 3.x notes:
        //  - WRITE_DATES_AS_TIMESTAMPS removed; dates-as-timestamps is disabled by default.
        //  - JavaTimeModule is built into jackson-databind 3.x; no explicit registration needed.
        return builder -> builder
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }
}
