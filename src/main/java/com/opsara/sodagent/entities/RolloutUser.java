package com.opsara.sodagent.entities;


import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "rollout_users", schema = "sodagent")
public class RolloutUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(name = "mobile_number", nullable = false)
    private String mobileNumber;

    @Column(name = "organisation_id", nullable = false)
    private Integer orgId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public void setMobileNumber(String mobileNumber) {
        if (mobileNumber != null) {
            String refined = mobileNumber.trim().replaceFirst("^0+", "");
            if (!refined.startsWith("+")) {
                refined = "+" + refined;
            }
            this.mobileNumber = refined;
        } else {
            this.mobileNumber = null;
        }
    }
    // Getters and setters
}