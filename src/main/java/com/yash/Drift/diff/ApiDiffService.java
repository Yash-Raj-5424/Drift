package com.yash.Drift.diff;

import com.yash.Drift.dto.ApiDiffResponse;
import com.yash.Drift.dto.BreakingChange;
import com.yash.Drift.dto.EndpointChange;
import com.yash.Drift.entity.ApiVersion;
import com.yash.Drift.parser.OpenApiParserService;
import com.yash.Drift.service.ApiVersionService;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import io.swagger.v3.oas.models.parameters.Parameter;
import java.util.*;
import java.util.function.Function;
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

    private List<String> getChanges(Operation oldOp, Operation newOp){
        List<String> changes = new java.util.ArrayList<>();
        if(!Objects.equals(oldOp.getSummary(), newOp.getSummary())) changes.add("Summary changed");
        if(!Objects.equals(oldOp.getDescription(), newOp.getDescription())) changes.add("Description changed");
        if(!Objects.equals(oldOp.getOperationId(), newOp.getOperationId())) changes.add("Operation ID changed");
        if(!Objects.equals(parameterKeys(oldOp), parameterKeys(newOp))) changes.add("Parameters changed");
        if(!Objects.equals(responseKeys(oldOp), responseKeys(newOp))) changes.add("Response status code changed");
        return changes;
    }

    private List<String> getChanges(OpenAPI oldApi, OpenAPI newApi, String endpoint){

        String[] parts = endpoint.split(" ", 2);
        String method = parts[0];
        String path = parts[1];

        PathItem oldPathItem = oldApi.getPaths().get(path);
        PathItem newPathItem = newApi.getPaths().get(path);

        if(oldPathItem == null || newPathItem == null) return List.of();

        Operation oldOp = getOperation(oldPathItem, method);
        Operation newOp = getOperation(newPathItem, method);

        if(oldOp == null || newOp == null) return List.of();

        return getChanges(oldOp, newOp);
    }

    private Map<String, Parameter> parameterMap(List<Parameter> parameters){
        if(parameters == null) return Map.of();

        return parameters.stream()
                .collect(Collectors.toMap(p -> p.getIn() + ":" + p.getName(),
                        Function.identity()
                ));
    }

    private List<String> getBreakingChanges(Operation oldOp, Operation newOp){

        List<String> breakingChanges = new ArrayList<>();
        Map<String, Parameter> oldParams = parameterMap(oldOp.getParameters());
        Map<String, Parameter> newParams = parameterMap(newOp.getParameters());

        // new req params added
        for(Parameter newParam : newParams.values()){

            String key = newParam.getIn() + ":" + newParam.getName();
            if(!oldParams.containsKey(key) && Boolean.TRUE.equals(newParam.getRequired()) )
                breakingChanges.add("Required parameter added: " + newParam.getName());
        }

        // existing ones becoming required
        for(String key : oldParams.keySet()){

            Parameter oldParam = oldParams.get(key);
            Parameter newParam = newParams.get(key);

            if(newParam != null){
                boolean oldRequired = Boolean.TRUE.equals(oldParam.getRequired());
                boolean newRequired = Boolean.TRUE.equals(newParam.getRequired());

                if(!oldRequired && newRequired)
                    breakingChanges.add("Parameter became required: " + newParam.getName());
            }
        }

        // params removed
        for(String key : oldParams.keySet()){
            if(!newParams.containsKey(key)){
                Parameter oldParam = oldParams.get(key);
                breakingChanges.add("Parameter removed: " + oldParam.getName());
            }
        }

        // request body changed to required
        RequestBody oldBody = oldOp.getRequestBody();
        RequestBody newBody = newOp.getRequestBody();

        boolean oldRequired = oldBody != null && Boolean.TRUE.equals(oldBody.getRequired());
        boolean newRequired = newBody != null && Boolean.TRUE.equals(newBody.getRequired());

        if(!oldRequired && newRequired)
            breakingChanges.add("Request body became required");

        // success response removed
        ApiResponses oldResponses = oldOp.getResponses();
        ApiResponses newResponses = newOp.getResponses();

        if(oldResponses != null){
            for(String code : oldResponses.keySet()){
                if(!newResponses.containsKey(code))
                    breakingChanges.add("Response removed: " + code);
            }
        }

        return breakingChanges;
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
        List<EndpointChange> endpointChanges = new ArrayList<>();
        List<BreakingChange> breakingChanges = new ArrayList<>();

        for(String endpoint: commonEndpoints){

            String[] parts = endpoint.split(" ", 2);
            String method = parts[0];
            String path = parts[1];

            PathItem oldPathItem = oldOpenApi.getPaths().get(path);
            PathItem newPathItem = newOpenApi.getPaths().get(path);

            Operation oldOp = getOperation(oldPathItem, method);
            Operation newOP = getOperation(newPathItem, method);

            List<String> changes = getChanges(oldOpenApi, newOpenApi, endpoint);
            List<String> breaking = getBreakingChanges(oldOp, newOP);

            if(!changes.isEmpty()){
                modifiedEndpoints.add(endpoint);
                endpointChanges.add(new EndpointChange(endpoint, changes));
            }

            for(String reason: breaking){
                breakingChanges.add(new BreakingChange(endpoint, reason));
            }
        }

        return new ApiDiffResponse(addedEndpoints, removedEndpoints, commonEndpoints,
                modifiedEndpoints, endpointChanges, breakingChanges);


    }
}
