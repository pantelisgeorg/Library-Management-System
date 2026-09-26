package com.library.management;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

@Configuration
public class RestApiConfig implements RepositoryRestConfigurer {

    @Override
    public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config, CorsRegistry cors) {
        // Serve the auto-generated repository REST endpoints under /api
        // (e.g. GET /api/books, GET /api/users, POST /api/authors)
        config.setBasePath("/api");
    }
}
