package com.fiap.mecanica.os.infra.notification;

import com.fiap.mecanica.os.domain.model.OrdemServico;
import com.fiap.mecanica.os.infra.seeding.entity.UserSeedEntity;
import com.fiap.mecanica.os.infra.seeding.entity.VeiculoSeedEntity;
import com.fiap.mecanica.os.infra.seeding.repository.UserSeedRepository;
import com.fiap.mecanica.os.infra.seeding.repository.VeiculoSeedRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Junta os dados de cliente (tabela {@code users}, mesmo UUID do cliente) e veículo, hoje
 * espalhados em tabelas seed separadas, num snapshot único pros eventos de notificação.
 */
@Component
@RequiredArgsConstructor
public class OsNotificationSnapshotAssembler {

  private final UserSeedRepository userSeedRepository;
  private final VeiculoSeedRepository veiculoSeedRepository;

  public record Snapshot(
      String clienteNome,
      String clienteEmail,
      String veiculoPlaca,
      String veiculoMarca,
      String veiculoModelo,
      Integer veiculoAno) {}

  public Snapshot montar(OrdemServico os) {
    UserSeedEntity cliente =
        userSeedRepository
            .findById(os.getClienteId())
            .orElseThrow(
                () -> new IllegalStateException("Cliente não encontrado id=" + os.getClienteId()));
    VeiculoSeedEntity veiculo =
        veiculoSeedRepository
            .findById(os.getVeiculoId())
            .orElseThrow(
                () -> new IllegalStateException("Veículo não encontrado id=" + os.getVeiculoId()));

    return new Snapshot(
        cliente.getNome(),
        cliente.getEmail(),
        veiculo.getPlaca(),
        veiculo.getMarca(),
        veiculo.getModelo(),
        veiculo.getAno());
  }
}
