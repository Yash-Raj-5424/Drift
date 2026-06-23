package com.yash.Drift.service;

import com.yash.Drift.diff.EndpointExtractor;
import com.yash.Drift.dto.ApiVersionResponse;
import com.yash.Drift.entity.ApiVersion;
import com.yash.Drift.entity.TrackedApi;
import com.yash.Drift.exception.ApiNotFoundException;
import com.yash.Drift.exception.ApiVersionNotFoundException;
import com.yash.Drift.exception.DuplicateApiException;
import com.yash.Drift.parser.OpenApiParserService;
import com.yash.Drift.repository.ApiVersionRepository;
import com.yash.Drift.repository.TrackedApiRepository;
import io.swagger.v3.oas.models.OpenAPI;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;

@Service
@AllArgsConstructor
public class ApiVersionService {

    private final ApiVersionRepository apiVersionRepository;
    private final OpenApiParserService parserService;
    private final TrackedApiRepository trackedApiRepository;
    private final EndpointExtractor endpointExtractor;

    private ApiVersionResponse mapVersionToResponse(ApiVersion version){
        return new ApiVersionResponse(
                version.getId(),
                version.getVersion(),
                version.getFileName(),
                version.getOpenApiVersion(),
                version.getUploadedAt()
        );
    }

    public ApiVersionResponse uploadVersion(Long apiId, String version, MultipartFile file) throws IOException {
        TrackedApi trackedApi = trackedApiRepository.findById(apiId)
                .orElseThrow(() -> new ApiNotFoundException("Tracked API with id " + apiId + " not found"));

        if(apiVersionRepository.existsByTrackedApiIdAndVersion(apiId, version)){
            throw new DuplicateApiException("Version " + version + " for API " + trackedApi.getName() + " already exists.");
        }

        // extract openapi version
        OpenAPI openAPI = parserService.parse(file);
        String openApiVersion = openAPI.getOpenapi();

        Set<String> endpoints = endpointExtractor.extract(openAPI);
        System.out.println("[ENDPOINTS: ] Extracted endpoints: " + endpoints);

        String content = new String(file.getBytes(), StandardCharsets.UTF_8);

        return mapVersionToResponse(apiVersionRepository.save(ApiVersion.builder()
                .version(version)
                .fileName(file.getOriginalFilename())
                .openApiVersion(openApiVersion)
                .specification(content)
                .trackedApi(trackedApi)
                .build()));
    }

    public void deleteVersion(Long id){
        ApiVersion version = apiVersionRepository.findById(id)
                .orElseThrow(() -> new ApiVersionNotFoundException("API version with id " + id + " not found"));
        apiVersionRepository.delete(version);
    }
}
