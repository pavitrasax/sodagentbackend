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

    @Query(value = "SELECT u.* " +
            "FROM sodagent.user_checklist_data u " +
            "JOIN sodagent.organisation_checklist oc ON u.organisation_checklist_id = oc.id " +
            "WHERE u.user_credential = :userCredential " +
            "  AND u.user_credential_type = :userCredentialType " +
            "  AND u.filled_for_period = :filledForPeriod " +
            "  AND oc.org_id = :orgId " +
            "ORDER BY oc.version DESC, oc.created_at DESC " +
            "LIMIT 1",
            nativeQuery = true)
    Optional<UserChecklistData> findLatestForUserAndPeriodAndOrg(
            @Param("userCredential") String userCredential,
            @Param("userCredentialType") String userCredentialType,
            @Param("filledForPeriod") String filledForPeriod,
            @Param("orgId") Integer orgId
    );

    Optional<UserChecklistData> findByFilledForPeriodAndStoreIdAndOrganisationChecklist_Id(String filledForPeriod, Integer integer, Long organisationChecklistId);
}