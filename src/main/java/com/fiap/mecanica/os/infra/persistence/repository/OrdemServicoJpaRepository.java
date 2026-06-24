package com.fiap.mecanica.os.infra.persistence.repository;

import com.fiap.mecanica.os.infra.persistence.entity.OrdemServicoEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrdemServicoJpaRepository extends JpaRepository<OrdemServicoEntity, UUID> {}
