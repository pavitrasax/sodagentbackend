package com.opsara.sodagent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EntityScan({"com.opsara.sodagent", "com.opsara.aaaservice.entities"})
@ComponentScan({"com.opsara.sodagent", "com.opsara.aaaservice.services"})
@EnableJpaRepositories({"com.opsara.sodagent.repositories", "com.opsara.aaaservice.repositories"})
@SpringBootApplication
public class SodAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(SodAgentApplication.class, args);
    }

}
