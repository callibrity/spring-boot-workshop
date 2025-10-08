package com.callibrity.spring.workshop;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableConfigurationProperties(SpringBootWorkshopProps.class)
public class SpringBootWorkshopConfig {

    private final SpringBootWorkshopProps props;

    public SpringBootWorkshopConfig(SpringBootWorkshopProps props) {
        this.props = props;
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(@NonNull CorsRegistry registry) {
                registry.addMapping("/api/**").combine(props.getCors());
            }
        };
    }

//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        return http
//                .cors(Customizer.withDefaults()) // Allow CORS preflight requests
//                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**")) // Disable CSRF for API endpoints
//                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
//                .authorizeHttpRequests(authz -> {
//                    authz.requestMatchers(EndpointRequest.toAnyEndpoint()).permitAll(); // Allow all actuator endpoints
//                    authz.requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll(); // Allow Swagger UI and API docs
//                    authz.requestMatchers("/api/**").fullyAuthenticated(); // Require authentication for API endpoints
//                    authz.anyRequest().denyAll(); // Deny all other requests
//                })
//                .build();
//    }
}