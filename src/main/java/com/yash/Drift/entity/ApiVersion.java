package com.yash.Drift.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "api_versions")
public class ApiVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String version;
    private String fileName;
    private String OpenApiVersion;

    @CreationTimestamp
    private LocalDateTime uploadedAt;

    @ManyToOne
    private TrackedApi trackedApi;
}
