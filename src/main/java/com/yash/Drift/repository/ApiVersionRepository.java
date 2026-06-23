package com.yash.Drift.repository;

import com.yash.Drift.entity.ApiVersion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApiVersionRepository extends JpaRepository<ApiVersion, Long> {

    List<ApiVersion> findByTrackedApiId(Long trackedApiId);
    boolean existsByTrackedApiIdAndVersion(Long trackedApiId, String version);
}
