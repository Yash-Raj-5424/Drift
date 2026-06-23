package com.yash.Drift.diff;

import com.yash.Drift.dto.ApiDiffResponse;
import com.yash.Drift.entity.ApiVersion;
import com.yash.Drift.parser.OpenApiParserService;
import com.yash.Drift.service.ApiVersionService;
import io.swagger.v3.oas.models.OpenAPI;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ApiDiffService {

    private final ApiVersionService apiVersionService;
    private final OpenApiParserService parserService;
    private final EndpointExtractor endpointExtractor;

    public ApiDiffResponse compare(Long oldVersionId, Long newVersionId){
        // fetch both versions
        ApiVersion oldVersion = apiVersionService.getApiVersionById(oldVersionId);
        ApiVersion newVersion = apiVersionService.getApiVersionById(newVersionId);

        // parse both specifications
        OpenAPI oldOpenApi = parserService.parse(oldVersion.getSpecification());
        OpenAPI newOpenApi= parserService.parse(newVersion.getSpecification());

        // extract endpoints
        Set<String> oldEndpoints = endpointExtractor.extract(oldOpenApi);
        Set<String> newEndpoints = endpointExtractor.extract(newOpenApi);

        System.out.println(oldEndpoints);
        System.out.println(newEndpoints);

        // compute differences
        Set<String> addedEndpoints = new HashSet<>(newEndpoints);
        addedEndpoints.removeAll(oldEndpoints);

        // compute removed endpoints
        Set<String> removedEndpoints = new HashSet<>(oldEndpoints);
        removedEndpoints.removeAll(newEndpoints);

        System.out.println("Added endpoints: " + addedEndpoints);
        System.out.println("Removed endpoints: " + removedEndpoints);

        return new ApiDiffResponse(addedEndpoints, removedEndpoints);


    }
}
