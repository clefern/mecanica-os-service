package com.fiap.mecanica.os.domain.model;

import com.fiap.mecanica.os.domain.enums.TipoItem;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemOrdemServico {

  private UUID id;
  private TipoItem tipo;
  private String descricao;
  private BigDecimal valorUnitario;
  private Integer quantidade;
  private UUID referenciaId;

  public BigDecimal getSubtotal() {
    if (valorUnitario == null || quantidade == null) return BigDecimal.ZERO;
    return valorUnitario.multiply(BigDecimal.valueOf(quantidade));
  }
}
