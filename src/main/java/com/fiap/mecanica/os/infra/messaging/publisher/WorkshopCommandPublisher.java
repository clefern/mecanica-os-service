package com.fiap.mecanica.os.infra.messaging.publisher;

import com.fiap.mecanica.os.application.saga.OsSagaCoordinator;
import com.fiap.mecanica.os.application.saga.command.IniciarExecucaoCommand;
import com.fiap.mecanica.os.infra.messaging.config.RabbitMqConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WorkshopCommandPublisher implements OsSagaCoordinator.IniciarExecucaoCommandPublisher {

  private final RabbitTemplate rabbitTemplate;

  @Override
  public void publicar(IniciarExecucaoCommand command) {
    rabbitTemplate.convertAndSend(
        RabbitMqConfig.EXCHANGE,
        RabbitMqConfig.RK_INICIAR_EXECUCAO,
        command);
    log.info("[MQ] IniciarExecucaoCommand publicado sagaId={}", command.sagaId());
  }
}
