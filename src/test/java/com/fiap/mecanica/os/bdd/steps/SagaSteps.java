package com.fiap.mecanica.os.bdd.steps;

import com.fiap.mecanica.os.application.saga.OsSagaCoordinator;
import com.fiap.mecanica.os.application.saga.event.ExecucaoFinalizadaEvent;
import com.fiap.mecanica.os.application.saga.event.FalhaNaExecucaoEvent;
import com.fiap.mecanica.os.application.saga.event.FalhaNaReservaEvent;
import com.fiap.mecanica.os.application.saga.event.PagamentoConfirmadoEvent;
import com.fiap.mecanica.os.application.saga.event.PagamentoRecusadoEvent;
import com.fiap.mecanica.os.application.saga.event.PecasReservadasEvent;
import com.fiap.mecanica.os.bdd.ScenarioContext;
import com.fiap.mecanica.os.infra.persistence.entity.SagaStateEntity;
import com.fiap.mecanica.os.infra.persistence.repository.SagaStateJpaRepository;
import io.cucumber.java.pt.Quando;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;

public class SagaSteps {

  @Autowired
  private OsSagaCoordinator coordinator;

  @Autowired
  private SagaStateJpaRepository sagaRepo;

  @Autowired
  private OsSagaCoordinator.ReservarPecasCommandPublisher reservarPecasPublisher;

  @Autowired
  private OsSagaCoordinator.IniciarExecucaoCommandPublisher iniciarExecucaoPublisher;

  @Autowired
  private ScenarioContext ctx;

  private UUID sagaIdParaOs() {
    UUID osId = ctx.getOsId();
    return sagaRepo.findAll().stream()
        .filter(s -> osId.equals(s.getOsId()))
        .map(SagaStateEntity::getId)
        .findFirst()
        .orElseThrow(() -> new IllegalStateException(
            "Nenhuma SagaState encontrada para osId=" + osId));
  }

  @Quando("o billing confirma o pagamento da OS")
  public void billingConfirmaPagamento() {
    UUID sagaId = sagaIdParaOs();
    coordinator.onPagamentoConfirmado(
        new PagamentoConfirmadoEvent(sagaId, ctx.getOsId(), UUID.randomUUID(), "mp-test-123"),
        reservarPecasPublisher);
  }

  @Quando("o billing recusa o pagamento da OS")
  public void billingRecusaPagamento() {
    UUID sagaId = sagaIdParaOs();
    coordinator.onPagamentoRecusado(
        new PagamentoRecusadoEvent(sagaId, ctx.getOsId(), UUID.randomUUID(), "saldo insuficiente"));
  }

  @Quando("o inventory confirma a reserva de peças da OS")
  public void inventoryConfirmaReserva() {
    UUID sagaId = sagaIdParaOs();
    coordinator.onPecasReservadas(
        new PecasReservadasEvent(sagaId, ctx.getOsId()),
        iniciarExecucaoPublisher);
  }

  @Quando("o inventory informa falha na reserva da OS")
  public void inventoryFalhaReserva() {
    UUID sagaId = sagaIdParaOs();
    coordinator.onFalhaNaReserva(
        new FalhaNaReservaEvent(sagaId, ctx.getOsId(), "estoque insuficiente"));
  }

  @Quando("o workshop conclui a execução do reparo da OS")
  public void workshopConclui() {
    UUID sagaId = sagaIdParaOs();
    coordinator.onExecucaoFinalizada(
        new ExecucaoFinalizadaEvent(sagaId, ctx.getOsId(), UUID.randomUUID()));
  }

  @Quando("o workshop informa falha na execução da OS")
  public void workshopFalha() {
    UUID sagaId = sagaIdParaOs();
    coordinator.onFalhaNaExecucao(
        new FalhaNaExecucaoEvent(sagaId, ctx.getOsId(), "mecânico indisponível"));
  }
}
