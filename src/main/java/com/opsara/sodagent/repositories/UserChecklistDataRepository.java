package com.opsara.sodagent.repositories;


import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import com.opsara.sodagent.entities.UserChecklistData;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface UserChecklistDataRepository extends JpaRepository<UserChecklistData, Long> {
    List<UserChecklistData> findByOrganisationChecklist_OrgIdAndFilledForPeriodTsBetween(
            Integer orgId,
            LocalDateTime start,
            LocalDateTime end
    );

    @Query("""
                SELECT ucd.userCredential, COUNT(ucd)
                FROM UserChecklistData ucd
                JOIN WhatsappUser wu ON ucd.userCredential = wu.mobileNumber
                WHERE wu.orgId = :orgId
                  AND ucd.filledForPeriodTs >= :fromDate
                GROUP BY ucd.userCredential
                ORDER BY COUNT(ucd) DESC
            """)
    List<Object[]> findTopActiveUsersByOrgIdAndLastMonth(@Param("orgId") Integer orgId, @Param("fromDate") LocalDateTime fromDate, Pageable pageable);
}