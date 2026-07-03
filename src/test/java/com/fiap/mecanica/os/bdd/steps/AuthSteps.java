package com.fiap.mecanica.os.bdd.steps;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.mecanica.os.bdd.ScenarioContext;
import io.cucumber.java.pt.Dado;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

public class AuthSteps {

  @Autowired
  private TestRestTemplate restTemplate;

  @Autowired
  private ScenarioContext ctx;

  @Autowired
  private ObjectMapper objectMapper;

  @Dado("que estou autenticado como {string} com senha {string}")
  public void autenticar(String email, String senha) throws Exception {
    String body = """
        {"email": "%s", "password": "%s"}
        """.formatted(email, senha);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    ResponseEntity<String> response = restTemplate.postForEntity(
        "/api/auth/login",
        new HttpEntity<>(body, headers),
        String.class);

    assertThat(response.getStatusCode().is2xxSuccessful())
        .as("Login deve retornar 2xx, status recebido: %s, body: %s",
            response.getStatusCode(), response.getBody())
        .isTrue();

    JsonNode json = objectMapper.readTree(response.getBody());
    ctx.setToken(json.get("accessToken").asText());
  }
}
