// UserChecklistData.java
package com.opsara.sodagent.entities;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Type;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(
        name = "user_checklist_data",
        schema = "sodagent",
        uniqueConstraints = @UniqueConstraint(
                name = "unique_user_checklist",
                columnNames = {"user_credential", "user_credential_type", "filled_for_period", "organisation_checklist_id"}
        )
)
public class UserChecklistData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_credential", nullable = false, length = 100)
    private String userCredential;

    @Column(name = "user_credential_type", nullable = false, length = 10)
    private String userCredentialType;

    @Column(name = "filled_for_period", nullable = false, length = 20)
    private String filledForPeriod;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "organisation_checklist_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_organisation_checklist")
    )
    private OrganisationChecklist organisationChecklist;

    @Type(JsonBinaryType.class)
    @Column(name = "data_json", nullable = false, columnDefinition = "jsonb")
    private String dataJson;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "filled_for_period_ts")
    private LocalDateTime filledForPeriodTs;
    // Getters and setters
    // (Omitted for brevity)


    // For compliance_score (NUMERIC(5,2))
    @Column(name = "compliance_score")
    private BigDecimal complianceScore;

    // For max_compliance_score (NUMERIC(5,2))
    @Column(name = "max_compliance_score")
    private BigDecimal maxComplianceScore;

    @Type(JsonBinaryType.class)
    @Column(name = "compliance_feedback", nullable = false, columnDefinition = "jsonb")
    private String complianceFeedback;


}