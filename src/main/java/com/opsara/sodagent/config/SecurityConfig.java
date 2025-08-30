package com.opsara.sodagent.config;


// src/main/java/com/example/security/SecurityConfig.java

import com.opsara.sodagent.security.JwtFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {
    @Autowired
    private JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {


        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/sodagent/getuniqueURL", "/sodagent/public/fillchecklist", "/sodagent/public/gettemplate").permitAll()
                        .requestMatchers("/sodagent/chat", "/sodagent/upload", "/sodagent/initialise").authenticated()
                        .anyRequest().permitAll()
                ).cors();

        /**http.csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                .requestMatchers("/api/auth/login", "/api/register").permitAll()
                .requestMatchers("/api/user/purchaseagent", "/api/user/getpurchasedagents").authenticated().anyRequest().permitAll();**/
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
