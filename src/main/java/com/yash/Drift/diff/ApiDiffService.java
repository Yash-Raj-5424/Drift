package com.yash.Drift.diff;

import com.yash.Drift.dto.ApiDiffResponse;
import com.yash.Drift.entity.ApiVersion;
import com.yash.Drift.parser.OpenApiParserService;
import com.yash.Drift.service.ApiVersionService;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApiDiffService {

    private final ApiVersionService apiVersionService;
    private final OpenApiParserService parserService;
    private final EndpointExtractor endpointExtractor;

    private Operation getOperation(PathItem pathItem, String method){
        return switch(method){
            case "GET" -> pathItem.getGet();
            case "POST" -> pathItem.getPost();
            case "PUT" -> pathItem.getPut();
            case "DELETE" -> pathItem.getDelete();
            case "PATCH" -> pathItem.getPatch();
            case "HEAD" -> pathItem.getHead();
            case "OPTIONS" -> pathItem.getOptions();
            default -> null;
        };
    }

    private Set<String> parameterKeys(Operation op){
        if(op.getParameters() == null) return Set.of();
        return op.getParameters().stream()
                .map(p -> p.getName() + "|" + p.getIn() + "|" + Boolean.TRUE.equals(p.getRequired()))
                .collect(Collectors.toSet());
    }

    private Set<String> responseKeys(Operation op){
        if(op.getResponses() == null) return Set.of();
        return op.getResponses().keySet();
    }

    private boolean isOperationModified(Operation oldOp, Operation newOp){
        if(!Objects.equals(oldOp.getSummary(), newOp.getSummary())) return true;
        if(!Objects.equals(oldOp.getDescription(), newOp.getDescription())) return true;
        if(!Objects.equals(oldOp.getOperationId(), newOp.getOperationId())) return true;
        if(!Objects.equals(parameterKeys(oldOp), parameterKeys(newOp))) return true;
        if(!Objects.equals(responseKeys(oldOp), responseKeys(newOp))) return true;
        return false;
    }

    private boolean isModified(OpenAPI oldApi, OpenAPI newApi, String endpoint){
        String[] parts = endpoint.split(" ", 2);
        String method = parts[0];
        String path = parts[1];

        PathItem oldPathItem = oldApi.getPaths().get(path);
        PathItem newPathItem = newApi.getPaths().get(path);
        if(oldPathItem == null || newPathItem == null) return false;

        Operation oldOp = getOperation(oldPathItem, method);
        Operation newOp = getOperation(newPathItem, method);
        if(oldOp == null || newOp == null) return false;

        return isOperationModified(oldOp, newOp);
    }

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

        // compute differences
        Set<String> addedEndpoints = new HashSet<>(newEndpoints);
        addedEndpoints.removeAll(oldEndpoints);

        // compute removed endpoints
        Set<String> removedEndpoints = new HashSet<>(oldEndpoints);
        removedEndpoints.removeAll(newEndpoints);

        // compute common endpoints
        Set<String> commonEndpoints = new HashSet<>(oldEndpoints);
        commonEndpoints.retainAll(newEndpoints);

        // compute modified endpoints
        Set<String> modifiedEndpoints = new HashSet<>();
        for(String endpoint: commonEndpoints){
            if(isModified(oldOpenApi, newOpenApi, endpoint)){
                modifiedEndpoints.add(endpoint);
            }
        }

        return new ApiDiffResponse(addedEndpoints, removedEndpoints, commonEndpoints, modifiedEndpoints);


    }
}
