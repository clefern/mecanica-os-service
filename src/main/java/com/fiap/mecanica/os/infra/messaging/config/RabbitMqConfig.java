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

  // M1 — inventory
  public static final String QUEUE_RESERVAR_PECAS = "mecanica.inventory.reservar-pecas";
  public static final String QUEUE_PECAS_RESERVADAS = "mecanica.os.pecas-reservadas";
  public static final String QUEUE_FALHA_RESERVA = "mecanica.os.falha-reserva";
  public static final String RK_RESERVAR_PECAS = "inventory.reservar-pecas";
  public static final String RK_PECAS_RESERVADAS = "os.pecas-reservadas";
  public static final String RK_FALHA_RESERVA = "os.falha-reserva";

  // M2 — billing
  public static final String QUEUE_GERAR_ORCAMENTO      = "mecanica.billing.gerar-orcamento";
  public static final String QUEUE_ORCAMENTO_CRIADO = "mecanica.os.orcamento-criado";
  public static final String QUEUE_FALHA_BILLING = "mecanica.os.falha-no-billing";
  public static final String QUEUE_PAGAMENTO_CONFIRMADO = "mecanica.os.pagamento-confirmado";
  public static final String QUEUE_PAGAMENTO_RECUSADO = "mecanica.os.pagamento-recusado";
  public static final String RK_GERAR_ORCAMENTO = "billing.gerar-orcamento";
  public static final String RK_ORCAMENTO_CRIADO = "os.orcamento-criado";
  public static final String RK_FALHA_BILLING = "os.falha-no-billing";
  public static final String RK_PAGAMENTO_CONFIRMADO = "os.pagamento-confirmado";
  public static final String RK_PAGAMENTO_RECUSADO = "os.pagamento-recusado";

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

  // M2 — billing queues (os-service é consumidor dessas 4 + publica em gerar-orcamento)
  @Bean
  Queue filaGerarOrcamento() {
    return QueueBuilder.durable(QUEUE_GERAR_ORCAMENTO).build();
  }

  @Bean
  Queue filaOrcamentoCriado() {
    return QueueBuilder.durable(QUEUE_ORCAMENTO_CRIADO).build();
  }

  @Bean
  Queue filaFalhaBilling() {
    return QueueBuilder.durable(QUEUE_FALHA_BILLING).build();
  }

  @Bean
  Queue filaPagamentoConfirmado() {
    return QueueBuilder.durable(QUEUE_PAGAMENTO_CONFIRMADO).build();
  }

  @Bean
  Queue filaPagamentoRecusado() {
    return QueueBuilder.durable(QUEUE_PAGAMENTO_RECUSADO).build();
  }

  @Bean
  Binding bindingGerarOrcamento(Queue filaGerarOrcamento, DirectExchange mecanicaExchange) {
    return BindingBuilder.bind(filaGerarOrcamento).to(mecanicaExchange).with(RK_GERAR_ORCAMENTO);
  }

  @Bean
  Binding bindingOrcamentoCriado(Queue filaOrcamentoCriado, DirectExchange mecanicaExchange) {
    return BindingBuilder.bind(filaOrcamentoCriado).to(mecanicaExchange).with(RK_ORCAMENTO_CRIADO);
  }

  @Bean
  Binding bindingFalhaBilling(Queue filaFalhaBilling, DirectExchange mecanicaExchange) {
    return BindingBuilder.bind(filaFalhaBilling).to(mecanicaExchange).with(RK_FALHA_BILLING);
  }

  @Bean
  Binding bindingPagamentoConfirmado(Queue filaPagamentoConfirmado, DirectExchange mecanicaExchange) {
    return BindingBuilder.bind(filaPagamentoConfirmado).to(mecanicaExchange).with(RK_PAGAMENTO_CONFIRMADO);
  }

  @Bean
  Binding bindingPagamentoRecusado(Queue filaPagamentoRecusado, DirectExchange mecanicaExchange) {
    return BindingBuilder.bind(filaPagamentoRecusado).to(mecanicaExchange).with(RK_PAGAMENTO_RECUSADO);
  }

  // M3 — workshop
  public static final String QUEUE_INICIAR_EXECUCAO    = "mecanica.workshop.iniciar-execucao";
  public static final String QUEUE_EXECUCAO_FINALIZADA = "mecanica.os.execucao-finalizada";
  public static final String QUEUE_FALHA_EXECUCAO      = "mecanica.os.falha-execucao";
  public static final String RK_INICIAR_EXECUCAO       = "workshop.iniciar-execucao";
  public static final String RK_EXECUCAO_FINALIZADA    = "os.execucao-finalizada";
  public static final String RK_FALHA_EXECUCAO         = "os.falha-execucao";

  @Bean
  Queue filaIniciarExecucao() {
    return QueueBuilder.durable(QUEUE_INICIAR_EXECUCAO).build();
  }

  @Bean
  Queue filaExecucaoFinalizada() {
    return QueueBuilder.durable(QUEUE_EXECUCAO_FINALIZADA).build();
  }

  @Bean
  Queue filaFalhaExecucao() {
    return QueueBuilder.durable(QUEUE_FALHA_EXECUCAO).build();
  }

  @Bean
  Binding bindingIniciarExecucao(Queue filaIniciarExecucao, DirectExchange mecanicaExchange) {
    return BindingBuilder.bind(filaIniciarExecucao).to(mecanicaExchange).with(RK_INICIAR_EXECUCAO);
  }

  @Bean
  Binding bindingExecucaoFinalizada(Queue filaExecucaoFinalizada, DirectExchange mecanicaExchange) {
    return BindingBuilder.bind(filaExecucaoFinalizada).to(mecanicaExchange).with(RK_EXECUCAO_FINALIZADA);
  }

  @Bean
  Binding bindingFalhaExecucao(Queue filaFalhaExecucao, DirectExchange mecanicaExchange) {
    return BindingBuilder.bind(filaFalhaExecucao).to(mecanicaExchange).with(RK_FALHA_EXECUCAO);
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
