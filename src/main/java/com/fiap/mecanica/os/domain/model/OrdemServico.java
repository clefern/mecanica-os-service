package com.fiap.mecanica.os.domain.model;

import com.fiap.mecanica.os.domain.enums.Prioridade;
import com.fiap.mecanica.os.domain.enums.StatusOS;
import com.fiap.mecanica.os.domain.enums.TipoItem;
import com.fiap.mecanica.os.domain.exception.ItemDuplicadoException;
import com.fiap.mecanica.os.domain.exception.TransicaoStatusInvalidaException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrdemServico {

  private UUID id;
  private UUID clienteId;
  private UUID veiculoId;
  private UUID mecanicoExecucaoId;
  private UUID mecanicoDiagnosticoId;
  private String codigo;
  private StatusOS status;
  private BigDecimal valorTotal;
  private LocalDateTime dataEntrada;
  private LocalDateTime dataPrevisao;
  private LocalDateTime dataFechamento;
  private LocalDateTime dataAprovacao;
  private String observacoes;
  private Prioridade prioridade;

  @Builder.Default
  private List<ItemOrdemServico> itens = new ArrayList<>();

  public static OrdemServico nova(UUID clienteId, UUID veiculoId) {
    return OrdemServico.builder()
        .clienteId(clienteId)
        .veiculoId(veiculoId)
        .codigo(gerarCodigo())
        .status(StatusOS.RECEBIDA)
        .dataEntrada(LocalDateTime.now())
        .valorTotal(BigDecimal.ZERO)
        .prioridade(Prioridade.NORMAL)
        .itens(new ArrayList<>())
        .build();
  }

  private static String gerarCodigo() {
    String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
    int suffix = ThreadLocalRandom.current().nextInt(10000);
    return "OS-%s-%04d".formatted(timestamp, suffix);
  }

  public void iniciarDiagnostico() {
    atualizarStatus(StatusOS.EM_DIAGNOSTICO);
  }

  public void emitirOrcamento() {
    if (this.itens.isEmpty()) {
      throw new TransicaoStatusInvalidaException("Não é possível emitir orçamento sem itens na OS");
    }
    atualizarStatus(StatusOS.AGUARDANDO_APROVACAO);
  }

  public void aprovar() {
    atualizarStatus(StatusOS.APROVADA);
    this.dataAprovacao = LocalDateTime.now();
  }

  public void iniciarExecucao() {
    if (this.mecanicoExecucaoId == null) {
      throw new TransicaoStatusInvalidaException(
          "Não é possível iniciar execução sem mecânico atribuído");
    }
    atualizarStatus(StatusOS.EM_EXECUCAO);
  }

  public void finalizar() {
    atualizarStatus(StatusOS.FINALIZADA);
  }

  public void entregar() {
    atualizarStatus(StatusOS.ENTREGUE);
  }

  public void cancelar() {
    atualizarStatus(StatusOS.CANCELADA);
  }

  public void adicionarItem(ItemOrdemServico item) {
    if (!canAddItem()) {
      throw new TransicaoStatusInvalidaException(
          "Não é possível adicionar itens a uma OS com status: " + this.status);
    }
    if (this.itens == null) this.itens = new ArrayList<>();
    boolean existe = this.itens.stream()
        .anyMatch(i -> i.getReferenciaId().equals(item.getReferenciaId()));
    if (existe) throw new ItemDuplicadoException(item.getDescricao(), item.getReferenciaId());
    this.itens.add(item);
    recalcularTotal();
  }

  public boolean canAddItem() {
    return this.status == null
        || this.status == StatusOS.RECEBIDA
        || this.status == StatusOS.EM_DIAGNOSTICO;
  }

  public List<ItemOrdemServico> itensDoPTipo(TipoItem tipo) {
    if (this.itens == null) return List.of();
    return this.itens.stream().filter(i -> i.getTipo() == tipo).toList();
  }

  private void atualizarStatus(StatusOS novoStatus) {
    validarTransicao(novoStatus);
    this.status = novoStatus;
    if (novoStatus == StatusOS.FINALIZADA
        || novoStatus == StatusOS.ENTREGUE
        || novoStatus == StatusOS.CANCELADA) {
      this.dataFechamento = LocalDateTime.now();
    }
  }

  private void validarTransicao(StatusOS novoStatus) {
    if (this.status == null) {
      if (novoStatus != StatusOS.RECEBIDA)
        throw new TransicaoStatusInvalidaException("Status inicial deve ser RECEBIDA");
      return;
    }
    if (this.status == novoStatus) return;
    if (this.status == StatusOS.CANCELADA || this.status == StatusOS.ENTREGUE)
      throw new TransicaoStatusInvalidaException(this.status.name(), novoStatus.name());
    if (novoStatus == StatusOS.CANCELADA) return;

    boolean valida = switch (this.status) {
      case RECEBIDA -> novoStatus == StatusOS.EM_DIAGNOSTICO;
      case EM_DIAGNOSTICO -> novoStatus == StatusOS.AGUARDANDO_APROVACAO;
      case AGUARDANDO_APROVACAO -> novoStatus == StatusOS.APROVADA
          || novoStatus == StatusOS.EM_DIAGNOSTICO;
      case APROVADA -> novoStatus == StatusOS.EM_EXECUCAO;
      case EM_EXECUCAO -> novoStatus == StatusOS.FINALIZADA;
      case FINALIZADA -> novoStatus == StatusOS.ENTREGUE;
      default -> false;
    };

    if (!valida)
      throw new TransicaoStatusInvalidaException(this.status.name(), novoStatus.name());
  }

  private void recalcularTotal() {
    if (this.itens == null || this.itens.isEmpty()) {
      this.valorTotal = BigDecimal.ZERO;
      return;
    }
    this.valorTotal = this.itens.stream()
        .map(ItemOrdemServico::getSubtotal)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }
}
