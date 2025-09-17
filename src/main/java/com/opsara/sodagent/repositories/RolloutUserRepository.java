package com.opsara.sodagent.repositories;

import com.opsara.sodagent.entities.RolloutUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RolloutUserRepository extends JpaRepository<RolloutUser, Long> {
    Optional<RolloutUser> findByMobileNumberAndOrgId(String mobileNumber, Integer orgId);// Add custom query methods if needed
}