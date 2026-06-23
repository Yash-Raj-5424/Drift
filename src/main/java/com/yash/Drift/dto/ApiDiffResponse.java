package com.yash.Drift.dto;

import java.util.Set;

public record ApiDiffResponse(
        Set<String> addedEndpoints,
        Set<String> removedEndpoints
){}

