package io.sendur.configuration;

import com.fasterxml.jackson.databind.*;
import io.sendur.factories.ObjectMapperFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfiguration {

    @Bean
    public ObjectMapper objectMapper() {
        return ObjectMapperFactory.create();
    }
}
