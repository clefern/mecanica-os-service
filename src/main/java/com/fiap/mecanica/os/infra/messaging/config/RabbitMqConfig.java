package com.fiap.mecanica.os.infra.messaging.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

  public static final String EXCHANGE = "mecanica.direct";
  public static final String QUEUE_RESERVAR_PECAS = "mecanica.inventory.reservar-pecas";
  public static final String QUEUE_PECAS_RESERVADAS = "mecanica.os.pecas-reservadas";
  public static final String QUEUE_FALHA_RESERVA = "mecanica.os.falha-reserva";
  public static final String RK_RESERVAR_PECAS = "inventory.reservar-pecas";
  public static final String RK_PECAS_RESERVADAS = "os.pecas-reservadas";
  public static final String RK_FALHA_RESERVA = "os.falha-reserva";

  @Bean
  DirectExchange mecanicaExchange() {
    return new DirectExchange(EXCHANGE, true, false);
  }

  @Bean
  Queue filaReservarPecas() {
    return QueueBuilder.durable(QUEUE_RESERVAR_PECAS).build();
  }

  @Bean
  Queue filaPecasReservadas() {
    return QueueBuilder.durable(QUEUE_PECAS_RESERVADAS).build();
  }

  @Bean
  Queue filaFalhaReserva() {
    return QueueBuilder.durable(QUEUE_FALHA_RESERVA).build();
  }

  @Bean
  Binding bindingReservarPecas(Queue filaReservarPecas, DirectExchange mecanicaExchange) {
    return BindingBuilder.bind(filaReservarPecas).to(mecanicaExchange).with(RK_RESERVAR_PECAS);
  }

  @Bean
  Binding bindingPecasReservadas(Queue filaPecasReservadas, DirectExchange mecanicaExchange) {
    return BindingBuilder.bind(filaPecasReservadas).to(mecanicaExchange).with(RK_PECAS_RESERVADAS);
  }

  @Bean
  Binding bindingFalhaReserva(Queue filaFalhaReserva, DirectExchange mecanicaExchange) {
    return BindingBuilder.bind(filaFalhaReserva).to(mecanicaExchange).with(RK_FALHA_RESERVA);
  }

  @Bean
  Jackson2JsonMessageConverter messageConverter(ObjectMapper objectMapper) {
    return new Jackson2JsonMessageConverter(objectMapper);
  }

  @Bean
  RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
      Jackson2JsonMessageConverter converter) {
    RabbitTemplate template = new RabbitTemplate(connectionFactory);
    template.setMessageConverter(converter);
    return template;
  }
}
