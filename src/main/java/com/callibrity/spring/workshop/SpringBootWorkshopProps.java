package com.callibrity.spring.workshop;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.web.cors.CorsConfiguration;

@Getter
@ConfigurationProperties(prefix="workshop")
public class SpringBootWorkshopProps {

    private final CorsConfiguration cors = new CorsConfiguration();

}
