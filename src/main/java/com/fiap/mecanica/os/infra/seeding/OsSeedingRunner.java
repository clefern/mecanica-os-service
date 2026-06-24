package com.fiap.mecanica.os.infra.seeding;

import com.fiap.mecanica.os.infra.seeding.entity.ClienteSeedEntity;
import com.fiap.mecanica.os.infra.seeding.entity.MecanicoSeedEntity;
import com.fiap.mecanica.os.infra.seeding.entity.UserSeedEntity;
import com.fiap.mecanica.os.infra.seeding.entity.VeiculoSeedEntity;
import com.fiap.mecanica.os.infra.seeding.repository.ClienteSeedRepository;
import com.fiap.mecanica.os.infra.seeding.repository.MecanicoSeedRepository;
import com.fiap.mecanica.os.infra.seeding.repository.UserSeedRepository;
import com.fiap.mecanica.os.infra.seeding.repository.VeiculoSeedRepository;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OsSeedingRunner implements CommandLineRunner {

  // UUIDs fixos para garantir referenciabilidade cross-service e nos testes
  public static final UUID ADMIN_ID =
      UUID.fromString("00000000-0000-0000-0000-000000000001");
  public static final UUID MECANICO_ID =
      UUID.fromString("00000000-0000-0000-0000-000000000002");
  public static final UUID ATENDENTE_ID =
      UUID.fromString("00000000-0000-0000-0000-000000000003");
  public static final UUID CLIENTE_ID =
      UUID.fromString("00000000-0000-0000-0000-000000000010");
  public static final UUID VEICULO_ID =
      UUID.fromString("00000000-0000-0000-0000-000000000020");

  private final UserSeedRepository userRepo;
  private final ClienteSeedRepository clienteRepo;
  private final MecanicoSeedRepository mecanicoRepo;
  private final VeiculoSeedRepository veiculoRepo;

  @Value("${seeding.enabled:true}")
  private boolean seedingEnabled;

  @Override
  public void run(String... args) {
    if (!seedingEnabled) {
      log.info("Seeding desabilitado.");
      return;
    }
    if (userRepo.existsByEmail("admin@mecanica.com")) {
      log.info("Seeding já executado — pulando.");
      return;
    }

    log.info("[SEED] Iniciando seeding do os-service...");
    String hash = new BCryptPasswordEncoder().encode("123456");
    LocalDateTime now = LocalDateTime.now();

    userRepo.save(UserSeedEntity.builder()
        .id(ADMIN_ID).nome("Admin").email("admin@mecanica.com")
        .passwordHash(hash).accountStatus(true).userType("ADMIN").role("ADMIN")
        .createdAt(now).updatedAt(now).build());

    userRepo.save(UserSeedEntity.builder()
        .id(MECANICO_ID).nome("Mecânico Seed").email("mecanico@mecanica.com")
        .passwordHash(hash).accountStatus(true).userType("MECANICO").role("MECANICO")
        .createdAt(now).updatedAt(now).build());

    mecanicoRepo.save(MecanicoSeedEntity.builder()
        .id(MECANICO_ID).cpf("87903980092").especialidade("Motor").build());

    userRepo.save(UserSeedEntity.builder()
        .id(ATENDENTE_ID).nome("Atendente Seed").email("atendente@mecanica.com")
        .passwordHash(hash).accountStatus(true).userType("ATENDENTE").role("ATENDENTE")
        .createdAt(now).updatedAt(now).build());

    userRepo.save(UserSeedEntity.builder()
        .id(CLIENTE_ID).nome("Cliente Seed").email("cliente@mecanica.com")
        .passwordHash(hash).accountStatus(true).userType("CLIENTE").role("CLIENTE")
        .createdAt(now).updatedAt(now).build());

    clienteRepo.save(ClienteSeedEntity.builder()
        .id(CLIENTE_ID).documento("45933904279").tipoPessoa("FISICA")
        .telefone("11987654321").endereco("Rua Seed, 42 - São Paulo/SP").build());

    veiculoRepo.save(VeiculoSeedEntity.builder()
        .id(VEICULO_ID).clienteId(CLIENTE_ID)
        .placa("ABC1D23").marca("Toyota").modelo("Corolla").ano(2022)
        .createdAt(now).updatedAt(now).build());

    log.info("[SEED] Seeding concluído: admin, mecanico, atendente, cliente, veiculo");
  }
}
