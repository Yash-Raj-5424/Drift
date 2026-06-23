package com.yash.Drift.controller;

import com.yash.Drift.service.ApiVersionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/versions")
@RequiredArgsConstructor
public class ApiVersionController {

    private final ApiVersionService apiVersionService;

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVersion(@PathVariable Long id){
        apiVersionService.deleteVersion(id);
        return ResponseEntity.noContent().build();
    }
}
