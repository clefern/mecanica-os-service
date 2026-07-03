package com.fiap.mecanica.os.bdd.steps;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.mecanica.os.bdd.ScenarioContext;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

public class OsLifecycleSteps {

  private static final UUID CLIENT_ID  = UUID.fromString("00000000-0000-0000-0000-000000000010");
  private static final UUID VEHICLE_ID = UUID.fromString("00000000-0000-0000-0000-000000000020");
  private static final UUID MECHANIC_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
  private static final UUID ITEM_REF   = UUID.fromString("10000000-0000-0000-0000-000000000002");

  @Autowired
  private TestRestTemplate restTemplate;

  @Autowired
  private ScenarioContext ctx;

  @Autowired
  private ObjectMapper objectMapper;

  // ── Helpers ───────────────────────────────────────────────────────────────

  private HttpHeaders authHeaders() {
    HttpHeaders h = new HttpHeaders();
    h.setContentType(MediaType.APPLICATION_JSON);
    h.setBearerAuth(ctx.getToken());
    return h;
  }

  private UUID criarOs(UUID clienteId, UUID veiculoId, UUID mecanicoId) throws Exception {
    String body = """
        {"clienteId": "%s", "veiculoId": "%s", "mecanicoId": "%s"}
        """.formatted(clienteId, veiculoId, mecanicoId);

    ResponseEntity<String> resp = restTemplate.postForEntity(
        "/api/ordens-servico",
        new HttpEntity<>(body, authHeaders()),
        String.class);

    assertThat(resp.getStatusCode().is2xxSuccessful())
        .as("Criar OS falhou: %s", resp.getBody()).isTrue();

    return UUID.fromString(objectMapper.readTree(resp.getBody()).get("id").asText());
  }

  private void adicionarItemNaOs(UUID osId, UUID referenciaId, String tipo,
      double valor, int qtd) throws Exception {
    String body = """
        {"referenciaId": "%s", "tipo": "%s", "descricao": "Item de teste", "valorUnitario": %s, "quantidade": %d}
        """.formatted(referenciaId, tipo, valor, qtd);

    restTemplate.exchange(
        "/api/ordens-servico/" + osId + "/itens",
        HttpMethod.POST,
        new HttpEntity<>(body, authHeaders()),
        String.class);
  }

  private void avancarStatusHttp(UUID osId, String endpoint) {
    restTemplate.exchange(
        "/api/ordens-servico/" + osId + "/" + endpoint,
        HttpMethod.PUT,
        new HttpEntity<>(authHeaders()),
        String.class);
  }

  UUID prepararOsNoStatus(String status) throws Exception {
    UUID osId = criarOs(CLIENT_ID, VEHICLE_ID, MECHANIC_ID);
    if ("RECEBIDA".equals(status)) return osId;

    adicionarItemNaOs(osId, ITEM_REF, "PECA", 189.90, 1);
    avancarStatusHttp(osId, "iniciar-diagnostico");
    if ("EM_DIAGNOSTICO".equals(status)) return osId;

    avancarStatusHttp(osId, "emitir-orcamento");
    if ("AGUARDANDO_APROVACAO".equals(status)) return osId;

    throw new IllegalArgumentException("Status não suportado como pré-condição: " + status);
  }

  // ── Steps ─────────────────────────────────────────────────────────────────

  @Quando("abro uma OS para o cliente {string} e veículo {string} e mecânico {string}")
  public void abrirOs(String clienteId, String veiculoId, String mecanicoId) throws Exception {
    UUID osId = criarOs(
        UUID.fromString(clienteId),
        UUID.fromString(veiculoId),
        UUID.fromString(mecanicoId));
    ctx.setOsId(osId);

    ResponseEntity<String> resp = restTemplate.exchange(
        "/api/ordens-servico/" + osId,
        HttpMethod.GET,
        new HttpEntity<>(authHeaders()),
        String.class);
    ctx.setLastResponse(resp);
  }

  @Dado("uma OS no status {string}")
  public void osNoStatus(String status) throws Exception {
    UUID osId = prepararOsNoStatus(status);
    ctx.setOsId(osId);
  }

  @Dado("uma OS no status {string} com um item adicionado")
  public void osNoStatusComItem(String status) throws Exception {
    UUID osId = prepararOsNoStatus(status);
    if ("EM_DIAGNOSTICO".equals(status)) {
      adicionarItemNaOs(osId, ITEM_REF, "PECA", 189.90, 1);
    }
    ctx.setOsId(osId);
  }

  @Quando("adiciono o item com referência {string} tipo {string} valor {double} quantidade {int}")
  public void adicionarItem(String refId, String tipo, double valor, int qtd) throws Exception {
    String body = """
        {"referenciaId": "%s", "tipo": "%s", "descricao": "Item de teste", "valorUnitario": %s, "quantidade": %d}
        """.formatted(refId, tipo, valor, qtd);

    ResponseEntity<String> resp = restTemplate.exchange(
        "/api/ordens-servico/" + ctx.getOsId() + "/itens",
        HttpMethod.POST,
        new HttpEntity<>(body, authHeaders()),
        String.class);
    ctx.setLastResponse(resp);
  }

  @Quando("tento adicionar um item à OS")
  public void tentarAdicionarItem() throws Exception {
    adicionarItem(ITEM_REF.toString(), "PECA", 100.0, 1);
  }

  @Quando("inicio o diagnóstico da OS")
  public void iniciarDiagnostico() {
    ResponseEntity<String> resp = restTemplate.exchange(
        "/api/ordens-servico/" + ctx.getOsId() + "/iniciar-diagnostico",
        HttpMethod.PUT,
        new HttpEntity<>(authHeaders()),
        String.class);
    ctx.setLastResponse(resp);
  }

  @Quando("emito o orçamento da OS")
  public void emitirOrcamento() {
    ResponseEntity<String> resp = restTemplate.exchange(
        "/api/ordens-servico/" + ctx.getOsId() + "/emitir-orcamento",
        HttpMethod.PUT,
        new HttpEntity<>(authHeaders()),
        String.class);
    ctx.setLastResponse(resp);
  }

  @Quando("cancelo a OS")
  public void cancelarOs() {
    ResponseEntity<String> resp = restTemplate.exchange(
        "/api/ordens-servico/" + ctx.getOsId() + "/cancelar",
        HttpMethod.PUT,
        new HttpEntity<>(authHeaders()),
        String.class);
    ctx.setLastResponse(resp);
  }

  @Entao("a OS é criada com status {string}")
  public void oscriadaComStatus(String statusEsperado) throws Exception {
    assertThat(ctx.getLastResponse().getStatusCode().is2xxSuccessful()).isTrue();
    JsonNode json = objectMapper.readTree(ctx.getLastResponse().getBody());
    assertThat(json.get("status").asText()).isEqualTo(statusEsperado);
  }

  @Entao("o código da OS começa com {string}")
  public void codigoComeçaCom(String prefixo) throws Exception {
    JsonNode json = objectMapper.readTree(ctx.getLastResponse().getBody());
    assertThat(json.get("codigo").asText()).startsWith(prefixo);
  }

  @Entao("o valor total da OS é {double}")
  public void valorTotalDaOs(double valorEsperado) throws Exception {
    assertThat(ctx.getLastResponse().getStatusCode().is2xxSuccessful()).isTrue();
    JsonNode json = objectMapper.readTree(ctx.getLastResponse().getBody());
    double valorReal = json.get("valorTotal").asDouble();
    assertThat(valorReal).isEqualByComparingTo(valorEsperado);
  }

  @Entao("a OS avança para o status {string}")
  public void osAvancaParaStatus(String statusEsperado) throws Exception {
    assertThat(ctx.getLastResponse().getStatusCode().is2xxSuccessful())
        .as("Resposta não-2xx: %s", ctx.getLastResponse().getBody()).isTrue();
    JsonNode json = objectMapper.readTree(ctx.getLastResponse().getBody());
    assertThat(json.get("status").asText()).isEqualTo(statusEsperado);
  }

  @Entao("a OS deve estar com status {string}")
  public void osDeveEstarComStatus(String statusEsperado) throws Exception {
    ResponseEntity<String> resp = restTemplate.exchange(
        "/api/ordens-servico/" + ctx.getOsId(),
        HttpMethod.GET,
        new HttpEntity<>(authHeaders()),
        String.class);
    assertThat(resp.getStatusCode().is2xxSuccessful()).isTrue();
    JsonNode json = objectMapper.readTree(resp.getBody());
    assertThat(json.get("status").asText())
        .as("Status esperado: %s, recebido: %s", statusEsperado, json.get("status").asText())
        .isEqualTo(statusEsperado);
  }

  @Entao("recebo resposta com status HTTP {int}")
  public void recebeRespostaComStatusHttp(int httpStatus) {
    assertThat(ctx.getLastResponse().getStatusCode().value()).isEqualTo(httpStatus);
  }
}
