package com.opsara.sodagent.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

import org.hibernate.annotations.Type;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;

@Data
@Entity
@Table(name = "organisation_checklist", schema = "sodagent", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"org_id", "version"})
})

public class OrganisationChecklist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "org_id", nullable = false)
    private Integer orgId;

    @Column(name = "version", nullable = false)
    private Integer version;

    @Type(JsonBinaryType.class)
    @Column(name = "checklist_json", columnDefinition = "jsonb", nullable = false)
    private String checklistJson;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = false;

    @Column(name = "status", nullable = false)
    private Integer status = 0;
    // Getters and setters omitted for brevity
}