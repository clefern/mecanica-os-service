package com.fiap.mecanica.os.bdd;

import com.fiap.mecanica.os.infra.messaging.publisher.BillingCommandPublisher;
import com.fiap.mecanica.os.infra.messaging.publisher.SagaCommandPublisher;
import com.fiap.mecanica.os.infra.messaging.publisher.WorkshopCommandPublisher;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.PostgreSQLContainer;

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@ContextConfiguration(initializers = CucumberSpringConfig.PostgresInitializer.class)
public class CucumberSpringConfig {

  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

  static {
    postgres.start();
  }

  public static class PostgresInitializer
      implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    @Override
    public void initialize(ConfigurableApplicationContext ctx) {
      TestPropertyValues.of(
          "spring.datasource.url=" + postgres.getJdbcUrl(),
          "spring.datasource.username=" + postgres.getUsername(),
          "spring.datasource.password=" + postgres.getPassword()
      ).applyTo(ctx.getEnvironment());
    }
  }

  @MockBean BillingCommandPublisher billingCommandPublisher;
  @MockBean SagaCommandPublisher sagaCommandPublisher;
  @MockBean WorkshopCommandPublisher workshopCommandPublisher;
}
