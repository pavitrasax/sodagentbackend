package com.opsara.sodagent.repositories;

import com.opsara.aaaservice.entities.StoreUser;
import com.opsara.sodagent.entities.RolloutUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository("sodRolloutUserRepository")
public interface RolloutUserRepository extends JpaRepository<RolloutUser, Long> {
    Optional<RolloutUser> findByMobileNumberAndOrgId(String mobileNumber, Integer orgId);// Add custom query methods if needed
    List<RolloutUser> findByOrgId(Integer orgId);
    List<RolloutUser> findByMobileNumber(String mobile);

}