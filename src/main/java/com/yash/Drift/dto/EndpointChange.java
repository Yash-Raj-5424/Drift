package com.yash.Drift.dto;

import java.util.List;

public record EndpointChange(
        String endpoint,
        List<String> changes
) {
}
