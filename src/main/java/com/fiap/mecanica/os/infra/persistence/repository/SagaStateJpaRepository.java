package com.fiap.mecanica.os.infra.persistence.repository;

import com.fiap.mecanica.os.infra.persistence.entity.SagaStateEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SagaStateJpaRepository extends JpaRepository<SagaStateEntity, UUID> {}
