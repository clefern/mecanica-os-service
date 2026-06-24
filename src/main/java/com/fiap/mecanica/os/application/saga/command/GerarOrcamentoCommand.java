package com.fiap.mecanica.os.application.saga.command;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record GerarOrcamentoCommand(
    UUID sagaId,
    UUID osId,
    List<ItemOrcamento> itens) {

  public record ItemOrcamento(
      UUID referenciaId,
      String tipo,
      String descricao,
      BigDecimal valorUnitario,
      int quantidade) {
  }
}
