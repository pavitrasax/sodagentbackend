package com.opsara.sodagent.repositories;


import org.springframework.data.jpa.repository.JpaRepository;
import com.opsara.sodagent.entities.UserChecklistData;

public interface UserChecklistDataRepository extends JpaRepository<UserChecklistData, Long> {
    // Add custom query methods if needed
}