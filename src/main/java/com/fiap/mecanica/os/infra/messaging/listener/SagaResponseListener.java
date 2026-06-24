package com.fiap.mecanica.os.infra.messaging.listener;

import com.fiap.mecanica.os.application.saga.OsSagaCoordinator;
import com.fiap.mecanica.os.application.saga.event.FalhaNaReservaEvent;
import com.fiap.mecanica.os.application.saga.event.PecasReservadasEvent;
import com.fiap.mecanica.os.infra.messaging.config.RabbitMqConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SagaResponseListener {

  private final OsSagaCoordinator sagaCoordinator;

  @RabbitListener(queues = RabbitMqConfig.QUEUE_PECAS_RESERVADAS)
  public void onPecasReservadas(PecasReservadasEvent event) {
    log.info("[MQ] Recebido PecasReservadasEvent sagaId={}", event.sagaId());
    sagaCoordinator.onPecasReservadas(event);
  }

  @RabbitListener(queues = RabbitMqConfig.QUEUE_FALHA_RESERVA)
  public void onFalhaNaReserva(FalhaNaReservaEvent event) {
    log.warn("[MQ] Recebido FalhaNaReservaEvent sagaId={}", event.sagaId());
    sagaCoordinator.onFalhaNaReserva(event);
  }
}
