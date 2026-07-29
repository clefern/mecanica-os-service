package com.fiap.mecanica.os.infra.messaging.listener;

import com.fiap.mecanica.os.application.messaging.OsFinalizadaNotificacaoEvent;
import com.fiap.mecanica.os.application.port.out.OrdemServicoRepositoryPort;
import com.fiap.mecanica.os.application.saga.OsSagaCoordinator;
import com.fiap.mecanica.os.application.saga.event.ExecucaoFinalizadaEvent;
import com.fiap.mecanica.os.application.saga.event.FalhaNaExecucaoEvent;
import com.fiap.mecanica.os.application.saga.event.FalhaNoBillingEvent;
import com.fiap.mecanica.os.application.saga.event.FalhaNaReservaEvent;
import com.fiap.mecanica.os.application.saga.event.OrcamentoCriadoEvent;
import com.fiap.mecanica.os.application.saga.event.PagamentoConfirmadoEvent;
import com.fiap.mecanica.os.application.saga.event.PagamentoRecusadoEvent;
import com.fiap.mecanica.os.application.saga.event.PecasReservadasEvent;
import com.fiap.mecanica.os.domain.model.OrdemServico;
import com.fiap.mecanica.os.infra.messaging.config.RabbitMqConfig;
import com.fiap.mecanica.os.infra.messaging.publisher.OsNotificationEventPublisher;
import com.fiap.mecanica.os.infra.messaging.publisher.SagaCommandPublisher;
import com.fiap.mecanica.os.infra.messaging.publisher.WorkshopCommandPublisher;
import com.fiap.mecanica.os.infra.notification.OsNotificationSnapshotAssembler;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SagaResponseListener {

  private final OsSagaCoordinator sagaCoordinator;
  private final SagaCommandPublisher sagaCommandPublisher;
  private final WorkshopCommandPublisher workshopCommandPublisher;
  private final OrdemServicoRepositoryPort ordemServicoRepository;
  private final OsNotificationSnapshotAssembler snapshotAssembler;
  private final OsNotificationEventPublisher notificationEventPublisher;

  // M1 — inventory responses
  @RabbitListener(queues = RabbitMqConfig.QUEUE_PECAS_RESERVADAS)
  public void onPecasReservadas(PecasReservadasEvent event) {
    log.info("[MQ] Recebido PecasReservadasEvent sagaId={}", event.sagaId());
    sagaCoordinator.onPecasReservadas(event, workshopCommandPublisher);
  }

  @RabbitListener(queues = RabbitMqConfig.QUEUE_FALHA_RESERVA)
  public void onFalhaNaReserva(FalhaNaReservaEvent event) {
    log.warn("[MQ] Recebido FalhaNaReservaEvent sagaId={}", event.sagaId());
    sagaCoordinator.onFalhaNaReserva(event);
  }

  // M2 — billing responses
  @RabbitListener(queues = RabbitMqConfig.QUEUE_ORCAMENTO_CRIADO)
  public void onOrcamentoCriado(OrcamentoCriadoEvent event) {
    log.info("[MQ] Recebido OrcamentoCriadoEvent sagaId={} orcamentoId={}", event.sagaId(), event.orcamentoId());
    sagaCoordinator.onOrcamentoCriado(event);
  }

  @RabbitListener(queues = RabbitMqConfig.QUEUE_FALHA_BILLING)
  public void onFalhaNoBilling(FalhaNoBillingEvent event) {
    log.warn("[MQ] Recebido FalhaNoBillingEvent sagaId={} motivo={}", event.sagaId(), event.motivo());
    sagaCoordinator.onFalhaNoBilling(event);
  }

  @RabbitListener(queues = RabbitMqConfig.QUEUE_PAGAMENTO_CONFIRMADO)
  public void onPagamentoConfirmado(PagamentoConfirmadoEvent event) {
    log.info("[MQ] Recebido PagamentoConfirmadoEvent sagaId={}", event.sagaId());
    sagaCoordinator.onPagamentoConfirmado(event, sagaCommandPublisher);
  }

  @RabbitListener(queues = RabbitMqConfig.QUEUE_PAGAMENTO_RECUSADO)
  public void onPagamentoRecusado(PagamentoRecusadoEvent event) {
    log.warn("[MQ] Recebido PagamentoRecusadoEvent sagaId={} motivo={}", event.sagaId(), event.motivo());
    sagaCoordinator.onPagamentoRecusado(event);
  }

  // M3 — workshop responses
  @RabbitListener(queues = RabbitMqConfig.QUEUE_EXECUCAO_FINALIZADA)
  public void onExecucaoFinalizada(ExecucaoFinalizadaEvent event) {
    log.info("[MQ] Recebido ExecucaoFinalizadaEvent sagaId={} execucaoId={}", event.sagaId(), event.execucaoId());
    sagaCoordinator.onExecucaoFinalizada(event);
    publicarNotificacaoFinalizacao(event.osId());
  }

  private void publicarNotificacaoFinalizacao(UUID osId) {
    ordemServicoRepository.buscarPorId(osId).ifPresentOrElse(this::publicarNotificacao,
        () -> log.error("[NOTIFICATION] OS não encontrada após finalização osId={}", osId));
  }

  private void publicarNotificacao(OrdemServico os) {
    OsNotificationSnapshotAssembler.Snapshot snapshot = snapshotAssembler.montar(os);
    notificationEventPublisher.publicar(
        new OsFinalizadaNotificacaoEvent(
            os.getId(),
            os.getCodigo(),
            os.getDataEntrada(),
            os.getClienteId(),
            snapshot.clienteNome(),
            snapshot.clienteEmail(),
            os.getVeiculoId(),
            snapshot.veiculoPlaca(),
            snapshot.veiculoMarca(),
            snapshot.veiculoModelo(),
            snapshot.veiculoAno()));
  }

  @RabbitListener(queues = RabbitMqConfig.QUEUE_FALHA_EXECUCAO)
  public void onFalhaNaExecucao(FalhaNaExecucaoEvent event) {
    log.warn("[MQ] Recebido FalhaNaExecucaoEvent sagaId={} motivo={}", event.sagaId(), event.motivo());
    sagaCoordinator.onFalhaNaExecucao(event);
  }
}
