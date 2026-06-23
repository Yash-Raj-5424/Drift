package com.yash.Drift.service;

import com.yash.Drift.dto.ApiVersionResponse;
import com.yash.Drift.dto.CreateTrackedApiRequest;
import com.yash.Drift.dto.TrackedApiResponse;
import com.yash.Drift.entity.ApiVersion;
import com.yash.Drift.entity.TrackedApi;
import com.yash.Drift.exception.ApiNotFoundException;
import com.yash.Drift.exception.DuplicateApiException;
import com.yash.Drift.parser.OpenApiParserService;
import com.yash.Drift.repository.ApiVersionRepository;
import com.yash.Drift.repository.TrackedApiRepository;
import io.swagger.v3.oas.models.OpenAPI;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@AllArgsConstructor
public class TrackedApiService {

    private final TrackedApiRepository trackedApiRepository;

    private TrackedApiResponse mapApiToResponse(TrackedApi api){
        return new TrackedApiResponse(
                api.getId(),
                api.getName(),
                api.getDescription()
        );
    }

    public TrackedApiResponse create(CreateTrackedApiRequest request){
        if(trackedApiRepository.existsByName(request.name())){
            throw new DuplicateApiException("Tracked API with name " + request.name() + " already exists.");
        }

        TrackedApi api = TrackedApi.builder()
                .name(request.name())
                .description(request.description())
                .build();
        api = trackedApiRepository.save(api);
        return mapApiToResponse(api);
    }

    public List<TrackedApiResponse> getAll(){

        return trackedApiRepository.findAll().stream()
                .map(this::mapApiToResponse)
                .toList();

    }

    public TrackedApiResponse getById(Long id){
        TrackedApi api = trackedApiRepository.findById(id)
                .orElseThrow(() -> new ApiNotFoundException("Tracked API with id " + id + " not found"));

        return mapApiToResponse(api);
    }

    public TrackedApi findEntityById(Long id){
        return trackedApiRepository.findById(id)
                .orElseThrow(() -> new ApiNotFoundException("Tracked API with id " + id + " not found"));
    }


}
