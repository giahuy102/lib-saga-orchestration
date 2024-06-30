package com.huyle.ms.saga.repository;

import com.huyle.ms.saga.entity.SagaInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SagaRepository extends JpaRepository<SagaInstance, UUID> {
}
