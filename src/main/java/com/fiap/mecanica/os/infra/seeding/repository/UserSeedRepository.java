package com.fiap.mecanica.os.infra.seeding.repository;

import com.fiap.mecanica.os.infra.seeding.entity.UserSeedEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserSeedRepository extends JpaRepository<UserSeedEntity, UUID> {
  boolean existsByEmail(String email);
}
