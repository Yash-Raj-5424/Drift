package com.yash.Drift.diff;

import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class EndpointExtractor {

    public Set<String> extract(OpenAPI openAPI) {

        Set<String> endpoints = new HashSet<>();

        openAPI
                .getPaths()
                .forEach((path, item) -> {
                    item.readOperationsMap().forEach((method, operation) -> {
                    endpoints.add(method.name() + " " + path); // Combine method and path
            });
        });

        return endpoints; // Return an empty set for now
    }
}
