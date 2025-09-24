package com.opsara.sodagent.config;


import com.opsara.aaaservice.config.AbstractSecurityConfig;
import org.springframework.context.annotation.Configuration;
import static com.opsara.aaaservice.constants.Constants.AUTH_ENDPOINTS;
import static com.opsara.aaaservice.constants.Constants.PUBLIC_ENDPOINTS;

@Configuration
public class SecurityConfig extends AbstractSecurityConfig {



    @Override
    protected String[] getPublicEndpoints() {
        return PUBLIC_ENDPOINTS;
    }

    @Override
    protected String[] getAuthEndpoints() {
        return AUTH_ENDPOINTS;
    }
}

