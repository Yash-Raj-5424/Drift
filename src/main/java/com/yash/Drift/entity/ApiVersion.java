package com.yash.Drift.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "api_versions")
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ApiVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String version;
    private String fileName;
    private String openApiVersion;

    @Column(columnDefinition = "TEXT")
    private String specification;

    @CreationTimestamp
    private LocalDateTime uploadedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="tracked_api_id")
    private TrackedApi trackedApi;
}
