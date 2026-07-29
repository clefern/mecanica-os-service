package com.fiap.mecanica.os.infra.messaging.publisher;

import com.fiap.mecanica.os.application.messaging.OsCriadaNotificacaoEvent;
import com.fiap.mecanica.os.application.messaging.OsFinalizadaNotificacaoEvent;
import com.fiap.mecanica.os.infra.messaging.config.RabbitMqConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OsNotificationEventPublisher {

  private final RabbitTemplate rabbitTemplate;

  public void publicar(OsCriadaNotificacaoEvent event) {
    rabbitTemplate.convertAndSend(
        RabbitMqConfig.EXCHANGE, RabbitMqConfig.RK_NOTIFICATION_OS_CRIADA, event);
    log.info("[MQ] OsCriadaNotificacaoEvent osId={}", event.osId());
  }

  public void publicar(OsFinalizadaNotificacaoEvent event) {
    rabbitTemplate.convertAndSend(
        RabbitMqConfig.EXCHANGE, RabbitMqConfig.RK_NOTIFICATION_OS_FINALIZADA, event);
    log.info("[MQ] OsFinalizadaNotificacaoEvent osId={}", event.osId());
  }
}
