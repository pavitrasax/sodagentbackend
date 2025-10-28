package com.opsara.sodagent.repositories;


import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import com.opsara.sodagent.entities.UserChecklistData;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserChecklistDataRepository extends JpaRepository<UserChecklistData, Long> {
    List<UserChecklistData> findByOrganisationChecklist_OrgIdAndFilledForPeriodTsBetween(
            Integer orgId,
            LocalDateTime start,
            LocalDateTime end
    );

    List<UserChecklistData> findByUserCredentialAndFilledForPeriodTsBetween(String userCredentials, LocalDateTime fromDateTime, LocalDateTime toDateTime);

    Optional<UserChecklistData> findByUserCredentialAndUserCredentialTypeAndFilledForPeriodAndOrganisationChecklist_Id(
            String userCredential,
            String userCredentialType,
            String filledForPeriod,
            Long organisationChecklistId
    );
}