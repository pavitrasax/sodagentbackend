package com.opsara.sodagent.repositories;


import org.springframework.data.jpa.repository.JpaRepository;
import com.opsara.sodagent.entities.UserChecklistData;

import java.time.LocalDateTime;
import java.util.List;

public interface UserChecklistDataRepository extends JpaRepository<UserChecklistData, Long> {
    List<UserChecklistData> findByOrganisationChecklist_OrgIdAndFilledForPeriodTsBetween(
            Integer orgId,
            LocalDateTime start,
            LocalDateTime end
    );
}