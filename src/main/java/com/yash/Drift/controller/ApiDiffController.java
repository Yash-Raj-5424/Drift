package com.yash.Drift.controller;

import com.yash.Drift.diff.ApiDiffService;
import com.yash.Drift.dto.ApiDiffResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/compare")
@RequiredArgsConstructor
public class ApiDiffController {

    private final ApiDiffService apiDiffService;

    @GetMapping("/{oldVersionId}/{newVersionId}")
    public ResponseEntity<ApiDiffResponse> compare(@PathVariable Long oldVersionId, @PathVariable Long newVersionId){
        return ResponseEntity.ok(apiDiffService.compare(oldVersionId, newVersionId));
    }

}
