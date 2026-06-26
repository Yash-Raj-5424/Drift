package com.yash.Drift.dto;

import java.util.List;
import java.util.Set;

public record ApiDiffResponse(
        Set<String> addedEndpoints,
        Set<String> removedEndpoints,
        Set<String> commonEndpoints,
        Set<String> modifiedEndpoints,
        List<EndpointChange> endpointChanges,
        List<BreakingChange> breakingChanges
){}

