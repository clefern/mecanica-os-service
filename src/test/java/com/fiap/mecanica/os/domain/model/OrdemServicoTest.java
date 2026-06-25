package com.fiap.mecanica.os.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fiap.mecanica.os.domain.enums.StatusOS;
import com.fiap.mecanica.os.domain.enums.TipoItem;
import com.fiap.mecanica.os.domain.exception.ItemDuplicadoException;
import com.fiap.mecanica.os.domain.exception.TransicaoStatusInvalidaException;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class OrdemServicoTest {

  private static final UUID CLIENTE_ID = UUID.randomUUID();
  private static final UUID VEICULO_ID = UUID.randomUUID();

  @Test
  void nova_deveIniciarComStatusRecebidaECodigoGerado() {
    OrdemServico os = OrdemServico.nova(CLIENTE_ID, VEICULO_ID);

    assertThat(os.getStatus()).isEqualTo(StatusOS.RECEBIDA);
    assertThat(os.getCodigo()).startsWith("OS-");
    assertThat(os.getValorTotal()).isEqualByComparingTo(BigDecimal.ZERO);
    assertThat(os.getItens()).isEmpty();
    assertThat(os.getDataEntrada()).isNotNull();
  }

  @Test
  void transicaoValida_recebida_paraEmDiagnostico() {
    OrdemServico os = OrdemServico.nova(CLIENTE_ID, VEICULO_ID);
    os.iniciarDiagnostico();
    assertThat(os.getStatus()).isEqualTo(StatusOS.EM_DIAGNOSTICO);
  }

  @Test
  void transicaoValida_emDiagnostico_paraAguardandoAprovacao() {
    OrdemServico os = criarOsComItem(StatusOS.EM_DIAGNOSTICO);
    os.emitirOrcamento();
    assertThat(os.getStatus()).isEqualTo(StatusOS.AGUARDANDO_APROVACAO);
  }

  @Test
  void transicaoValida_aguardandoAprovacao_paraAprovada() {
    OrdemServico os = criarOsComItem(StatusOS.AGUARDANDO_APROVACAO);
    os.aprovar();
    assertThat(os.getStatus()).isEqualTo(StatusOS.APROVADA);
    assertThat(os.getDataAprovacao()).isNotNull();
  }

  @Test
  void transicaoValida_aprovada_paraEmExecucao() {
    OrdemServico os = criarOsComItem(StatusOS.APROVADA);
    os.setMecanicoExecucaoId(UUID.randomUUID());
    os.iniciarExecucao();
    assertThat(os.getStatus()).isEqualTo(StatusOS.EM_EXECUCAO);
  }

  @Test
  void transicaoValida_emExecucao_paraFinalizada() {
    OrdemServico os = criarOsComItem(StatusOS.EM_EXECUCAO);
    os.finalizar();
    assertThat(os.getStatus()).isEqualTo(StatusOS.FINALIZADA);
    assertThat(os.getDataFechamento()).isNotNull();
  }

  @Test
  void transicaoValida_finalizada_paraEntregue() {
    OrdemServico os = criarOsComItem(StatusOS.FINALIZADA);
    os.entregar();
    assertThat(os.getStatus()).isEqualTo(StatusOS.ENTREGUE);
  }

  @Test
  void cancelar_podeSerChamadoDeQualquerStatusAtivo() {
    OrdemServico os = OrdemServico.nova(CLIENTE_ID, VEICULO_ID);
    os.cancelar();
    assertThat(os.getStatus()).isEqualTo(StatusOS.CANCELADA);
    assertThat(os.getDataFechamento()).isNotNull();
  }

  @Test
  void transicaoInvalida_deveLancarException() {
    OrdemServico os = OrdemServico.nova(CLIENTE_ID, VEICULO_ID);
    assertThatThrownBy(os::aprovar)
        .isInstanceOf(TransicaoStatusInvalidaException.class);
  }

  @Test
  void cancelar_aposEntregue_deveProibirNovaTransicao() {
    OrdemServico os = criarOsComItem(StatusOS.ENTREGUE);
    assertThatThrownBy(os::cancelar)
        .isInstanceOf(TransicaoStatusInvalidaException.class);
  }

  @Test
  void cancelar_aposCancelada_naoPodeTransicionar() {
    OrdemServico os = criarOsComItem(StatusOS.CANCELADA);
    assertThatThrownBy(os::iniciarDiagnostico)
        .isInstanceOf(TransicaoStatusInvalidaException.class);
  }

  @Test
  void emitirOrcamento_semItens_deveProibir() {
    OrdemServico os = criarOs(StatusOS.EM_DIAGNOSTICO);
    assertThatThrownBy(os::emitirOrcamento)
        .isInstanceOf(TransicaoStatusInvalidaException.class)
        .hasMessageContaining("sem itens");
  }

  @Test
  void iniciarExecucao_semMecanico_deveProibir() {
    OrdemServico os = criarOs(StatusOS.APROVADA);
    assertThatThrownBy(os::iniciarExecucao)
        .isInstanceOf(TransicaoStatusInvalidaException.class)
        .hasMessageContaining("mecânico");
  }

  @Test
  void adicionarItem_duplicado_deveProibir() {
    OrdemServico os = OrdemServico.nova(CLIENTE_ID, VEICULO_ID);
    UUID refId = UUID.randomUUID();
    ItemOrdemServico item = buildItem(refId, TipoItem.PECA, new BigDecimal("100.00"), 1);
    os.adicionarItem(item);
    assertThatThrownBy(() -> os.adicionarItem(item))
        .isInstanceOf(ItemDuplicadoException.class);
  }

  @Test
  void adicionarItem_statusInvalido_deveProibir() {
    OrdemServico os = criarOs(StatusOS.APROVADA);
    ItemOrdemServico item = buildItem(UUID.randomUUID(), TipoItem.PECA, new BigDecimal("50.00"), 1);
    assertThatThrownBy(() -> os.adicionarItem(item))
        .isInstanceOf(TransicaoStatusInvalidaException.class);
  }

  @Test
  void adicionarItem_deveRecalcularValorTotal() {
    OrdemServico os = OrdemServico.nova(CLIENTE_ID, VEICULO_ID);
    os.adicionarItem(buildItem(UUID.randomUUID(), TipoItem.PECA, new BigDecimal("100.00"), 2));
    os.adicionarItem(buildItem(UUID.randomUUID(), TipoItem.SERVICO, new BigDecimal("50.00"), 1));
    assertThat(os.getValorTotal()).isEqualByComparingTo(new BigDecimal("250.00"));
  }

  @Test
  void itensDoPTipo_deveFiltraTipoCorretamente() {
    OrdemServico os = OrdemServico.nova(CLIENTE_ID, VEICULO_ID);
    os.adicionarItem(buildItem(UUID.randomUUID(), TipoItem.PECA, new BigDecimal("10.00"), 1));
    os.adicionarItem(buildItem(UUID.randomUUID(), TipoItem.SERVICO, new BigDecimal("20.00"), 1));
    os.adicionarItem(buildItem(UUID.randomUUID(), TipoItem.INSUMO, new BigDecimal("5.00"), 1));

    assertThat(os.itensDoPTipo(TipoItem.PECA)).hasSize(1);
    assertThat(os.itensDoPTipo(TipoItem.SERVICO)).hasSize(1);
    assertThat(os.itensDoPTipo(TipoItem.INSUMO)).hasSize(1);
  }

  // ── helpers ─────────────────────────────────────────────────────────────

  private OrdemServico criarOs(StatusOS status) {
    return OrdemServico.builder()
        .id(UUID.randomUUID())
        .clienteId(CLIENTE_ID)
        .veiculoId(VEICULO_ID)
        .codigo("OS-TEST-0001")
        .status(status)
        .valorTotal(BigDecimal.ZERO)
        .build();
  }

  private OrdemServico criarOsComItem(StatusOS status) {
    OrdemServico os = criarOs(status);
    os.setItens(new java.util.ArrayList<>());
    os.getItens().add(buildItem(UUID.randomUUID(), TipoItem.SERVICO, new BigDecimal("100.00"), 1));
    return os;
  }

  private ItemOrdemServico buildItem(UUID refId, TipoItem tipo, BigDecimal valor, int qtd) {
    return ItemOrdemServico.builder()
        .id(UUID.randomUUID())
        .referenciaId(refId)
        .tipo(tipo)
        .descricao("item-" + tipo)
        .valorUnitario(valor)
        .quantidade(qtd)
        .build();
  }
}
