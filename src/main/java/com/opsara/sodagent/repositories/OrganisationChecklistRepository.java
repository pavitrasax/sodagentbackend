// src/main/java/com/opsara/sodagent/repository/OrganisationChecklistRepository.java
package com.opsara.sodagent.repositories;

import com.opsara.sodagent.entities.OrganisationChecklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository("sodOrganisationChecklistRepository")
public interface OrganisationChecklistRepository extends JpaRepository<OrganisationChecklist, Long> {
    List<OrganisationChecklist> findByOrgId(Integer orgId);

    List<OrganisationChecklist> findByOrgIdAndIsActiveTrue(Integer orgId);

    List<OrganisationChecklist> findByOrgIdAndIsActiveOrderByCreatedAtDesc(Integer orgId, boolean isActive);

    boolean existsByOrgId(Integer orgId);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query(value = "UPDATE sodagent.organisation_checklist SET status = 2 WHERE status = 1 AND org_id = :orgId", nativeQuery = true)
    void markStatusAsTwo(@Param("orgId") Integer orgId);


    @Modifying(clearAutomatically = true)
    @Transactional
    @Query(
            value = "UPDATE sodagent.organisation_checklist " +
                    "SET status = CASE WHEN status = 1 THEN 2 WHEN status = 3 THEN 5 ELSE status END " +
                    "WHERE org_id = :orgId AND status IN (1,3)",
            nativeQuery = true
    )
    int updateStatusesForMarkingEditedByUser(@Param("orgId") Integer orgId);

}
