package com.opsara.sodagent.repositories;


import org.springframework.data.jpa.repository.JpaRepository;
import com.opsara.sodagent.entities.WhatsappUser;

import java.util.List;

public interface WhatsappUserRepository extends JpaRepository<WhatsappUser, Long> {
    List<WhatsappUser> findByOrgId(Integer orgId);
}