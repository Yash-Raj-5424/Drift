package com.yash.Drift.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateTrackedApiRequest(

        @NotBlank
        String name,
        String description
){}
