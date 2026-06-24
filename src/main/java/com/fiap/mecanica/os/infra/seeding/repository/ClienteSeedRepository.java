package com.fiap.mecanica.os.infra.seeding.repository;

import com.fiap.mecanica.os.infra.seeding.entity.ClienteSeedEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteSeedRepository extends JpaRepository<ClienteSeedEntity, UUID> {}
