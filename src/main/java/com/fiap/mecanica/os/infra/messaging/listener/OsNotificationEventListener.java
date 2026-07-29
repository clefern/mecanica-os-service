package com.fiap.mecanica.os.infra.messaging.listener;

import com.fiap.mecanica.os.application.event.OsAbertaDomainEvent;
import com.fiap.mecanica.os.application.messaging.OsCriadaNotificacaoEvent;
import com.fiap.mecanica.os.application.port.out.OrdemServicoRepositoryPort;
import com.fiap.mecanica.os.domain.model.OrdemServico;
import com.fiap.mecanica.os.infra.messaging.publisher.OsNotificationEventPublisher;
import com.fiap.mecanica.os.infra.notification.OsNotificationSnapshotAssembler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Publica a notificação de "OS criada" só depois que a transação HTTP de abertura commitou —
 * evita acoplar a resposta da API à disponibilidade do RabbitMQ.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OsNotificationEventListener {

  private final OrdemServicoRepositoryPort repository;
  private final OsNotificationSnapshotAssembler snapshotAssembler;
  private final OsNotificationEventPublisher publisher;

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onOsAberta(OsAbertaDomainEvent event) {
    repository
        .buscarPorId(event.osId())
        .ifPresentOrElse(this::publicarNotificacao,
            () -> log.error("[NOTIFICATION] OS não encontrada após commit osId={}", event.osId()));
  }

  private void publicarNotificacao(OrdemServico os) {
    OsNotificationSnapshotAssembler.Snapshot snapshot = snapshotAssembler.montar(os);
    publisher.publicar(
        new OsCriadaNotificacaoEvent(
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
}
