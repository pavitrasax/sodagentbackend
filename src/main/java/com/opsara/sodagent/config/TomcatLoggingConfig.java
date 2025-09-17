package com.opsara.sodagent.config;

import org.apache.catalina.valves.ValveBase;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class TomcatLoggingConfig {

    private static final Logger logger = LoggerFactory.getLogger(TomcatLoggingConfig.class);

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> tomcatCustomizer() {
        return factory -> factory.addContextValves(new ValveBase() {
            @Override
            public void invoke(Request request, Response response) {
                logger.info("Tomcat received request: {}", request.getRequestURI());
                try {
                    getNext().invoke(request, response);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}