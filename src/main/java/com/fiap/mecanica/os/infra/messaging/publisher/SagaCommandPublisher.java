package com.fiap.mecanica.os.infra.messaging.publisher;

import com.fiap.mecanica.os.application.saga.OsSagaCoordinator;
import com.fiap.mecanica.os.application.saga.command.ReservarPecasCommand;
import com.fiap.mecanica.os.infra.messaging.config.RabbitMqConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SagaCommandPublisher implements OsSagaCoordinator.ReservarPecasCommandPublisher {

  private final RabbitTemplate rabbitTemplate;

  @Override
  public void publicar(ReservarPecasCommand command) {
    rabbitTemplate.convertAndSend(
        RabbitMqConfig.EXCHANGE,
        RabbitMqConfig.RK_RESERVAR_PECAS,
        command);
    log.info("[MQ] ReservarPecasCommand publicado sagaId={}", command.sagaId());
  }
}
