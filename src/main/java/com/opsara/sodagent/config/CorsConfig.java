package com.opsara.sodagent.config;

import com.opsara.aaaservice.config.AbstractCorsConfig;
import org.springframework.context.annotation.Configuration;
import static com.opsara.sodagent.constants.Constants.ALLOWED_ORIGINS;


@Configuration
public class CorsConfig extends AbstractCorsConfig {
    @Override
    protected String[] getAllowedOrigins() {
        return ALLOWED_ORIGINS;
    }
}