package com.fiap.mecanica.os.infra.messaging.publisher;

import com.fiap.mecanica.os.application.saga.OsSagaCoordinator;
import com.fiap.mecanica.os.application.saga.command.GerarOrcamentoCommand;
import com.fiap.mecanica.os.infra.messaging.config.RabbitMqConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BillingCommandPublisher implements OsSagaCoordinator.GerarOrcamentoCommandPublisher {

  private final RabbitTemplate rabbitTemplate;

  @Override
  public void publicar(GerarOrcamentoCommand command) {
    rabbitTemplate.convertAndSend(
        RabbitMqConfig.EXCHANGE,
        RabbitMqConfig.RK_GERAR_ORCAMENTO,
        command);
    log.info("[MQ] GerarOrcamentoCommand publicado sagaId={}", command.sagaId());
  }
}
