package com.opsara.sodagent.repositories;



import org.springframework.data.jpa.repository.JpaRepository;
import com.opsara.sodagent.entities.WhatsappUser;

public interface WhatsappUserRepository extends JpaRepository<WhatsappUser, Long> {
}