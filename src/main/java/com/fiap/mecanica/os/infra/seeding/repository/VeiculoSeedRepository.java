package com.fiap.mecanica.os.infra.seeding.repository;

import com.fiap.mecanica.os.infra.seeding.entity.VeiculoSeedEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VeiculoSeedRepository extends JpaRepository<VeiculoSeedEntity, UUID> {}
