package com.yash.Drift.controller;

import com.yash.Drift.dto.ApiVersionResponse;
import com.yash.Drift.dto.CreateTrackedApiRequest;
import com.yash.Drift.dto.TrackedApiResponse;
import com.yash.Drift.parser.OpenApiParserService;
import com.yash.Drift.service.ApiVersionService;
import com.yash.Drift.service.TrackedApiService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/apis")
@RequiredArgsConstructor
public class TrackedApiController{

    private final TrackedApiService trackedApiService;
    private final ApiVersionService apiVersionService;

    @PostMapping
    public ResponseEntity<TrackedApiResponse> createApi(@RequestBody @Valid CreateTrackedApiRequest request){
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(trackedApiService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<TrackedApiResponse>> getAllApis() {
        return ResponseEntity.ok(trackedApiService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TrackedApiResponse> getApiById(@PathVariable Long id){
        return ResponseEntity.ok(trackedApiService.getById(id));
    }

    @PostMapping(value = "/{id}/versions", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiVersionResponse> uploadVersion(@PathVariable Long id, @RequestParam String version,
                                                               @RequestParam MultipartFile file) throws IOException {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(apiVersionService.uploadVersion(id, version, file));
    }


}
