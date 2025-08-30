package com.opsara.sodagent.repositories;



import org.springframework.data.jpa.repository.JpaRepository;
import com.opsara.sodagent.entities.Store;

public interface StoreRepository extends JpaRepository<Store, Long> {
}