package com.yash.Drift.repository;

import com.yash.Drift.entity.TrackedApi;
import org.springframework.data.jpa.repository.JpaRepository;



public interface TrackedApiRepository extends JpaRepository<TrackedApi, Long> {

    boolean existsByName(String name);
}
