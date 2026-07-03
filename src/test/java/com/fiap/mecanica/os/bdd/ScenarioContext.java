package com.fiap.mecanica.os.bdd;

import io.cucumber.spring.ScenarioScope;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@ScenarioScope
public class ScenarioContext {

  private String token;
  private UUID osId;
  private UUID sagaId;
  private ResponseEntity<String> lastResponse;

  public String getToken() { return token; }
  public void setToken(String token) { this.token = token; }

  public UUID getOsId() { return osId; }
  public void setOsId(UUID osId) { this.osId = osId; }

  public UUID getSagaId() { return sagaId; }
  public void setSagaId(UUID sagaId) { this.sagaId = sagaId; }

  public ResponseEntity<String> getLastResponse() { return lastResponse; }
  public void setLastResponse(ResponseEntity<String> lastResponse) { this.lastResponse = lastResponse; }
}
