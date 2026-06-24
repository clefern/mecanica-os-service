package com.fiap.mecanica.os.presentation.dto;

import com.fiap.mecanica.os.domain.model.ItemOrdemServico;
import com.fiap.mecanica.os.domain.model.OrdemServico;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrdemServicoResponse(
    UUID id,
    UUID clienteId,
    UUID veiculoId,
    String codigo,
    String status,
    String prioridade,
    BigDecimal valorTotal,
    LocalDateTime dataEntrada,
    LocalDateTime dataAprovacao,
    LocalDateTime dataFechamento,
    List<ItemResponse> itens) {

  public record ItemResponse(
      UUID id, String tipo, String descricao, BigDecimal valorUnitario,
      int quantidade, BigDecimal subtotal, UUID referenciaId) {}

  public static OrdemServicoResponse from(OrdemServico os) {
    List<ItemResponse> itens = os.getItens() == null ? List.of() :
        os.getItens().stream()
            .map(i -> new ItemResponse(
                i.getId(), i.getTipo().name(), i.getDescricao(),
                i.getValorUnitario(), i.getQuantidade(), i.getSubtotal(), i.getReferenciaId()))
            .toList();
    return new OrdemServicoResponse(
        os.getId(), os.getClienteId(), os.getVeiculoId(),
        os.getCodigo(), os.getStatus().name(),
        os.getPrioridade() != null ? os.getPrioridade().name() : null,
        os.getValorTotal(), os.getDataEntrada(),
        os.getDataAprovacao(), os.getDataFechamento(), itens);
  }
}
