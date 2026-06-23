package com.yash.Drift.dto;

import java.time.LocalDateTime;

public record ApiVersionResponse (
        Long id,
        String version,
        String fileName,
        String openApiVersion,
        LocalDateTime uploadedAt
){}
