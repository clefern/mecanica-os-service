package com.fiap.mecanica.os.bdd;

import com.fiap.mecanica.os.infra.messaging.publisher.BillingCommandPublisher;
import com.fiap.mecanica.os.infra.messaging.publisher.SagaCommandPublisher;
import com.fiap.mecanica.os.infra.messaging.publisher.WorkshopCommandPublisher;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class CucumberSpringConfig {

  // Container estático: compartilhado entre todos os cenários (inicia uma vez)
  static PostgreSQLContainer<?> postgres =
      new PostgreSQLContainer<>("postgres:16-alpine");

  static {
    postgres.start();
    System.setProperty("spring.datasource.url", postgres.getJdbcUrl());
    System.setProperty("spring.datasource.username", postgres.getUsername());
    System.setProperty("spring.datasource.password", postgres.getPassword());
  }

  // Substitui os publishers reais (RabbitMQ) por mocks — sem conexão de rede
  @MockBean
  BillingCommandPublisher billingCommandPublisher;

  @MockBean
  SagaCommandPublisher sagaCommandPublisher;

  @MockBean
  WorkshopCommandPublisher workshopCommandPublisher;
}
