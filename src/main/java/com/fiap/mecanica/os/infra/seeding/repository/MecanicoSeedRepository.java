package com.fiap.mecanica.os.infra.seeding.repository;

import com.fiap.mecanica.os.infra.seeding.entity.MecanicoSeedEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MecanicoSeedRepository extends JpaRepository<MecanicoSeedEntity, UUID> {}
