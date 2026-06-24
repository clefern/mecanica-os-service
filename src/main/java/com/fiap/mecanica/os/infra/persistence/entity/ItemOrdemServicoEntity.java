package com.fiap.mecanica.os.infra.persistence.entity;

import com.fiap.mecanica.os.domain.enums.TipoItem;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "itens_ordem_servico")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemOrdemServicoEntity {

  @Id
  private UUID id;

  @ManyToOne
  @JoinColumn(name = "ordem_servico_id", nullable = false)
  private OrdemServicoEntity ordemServico;

  @Enumerated(EnumType.STRING)
  @Column(name = "tipo", nullable = false)
  private TipoItem tipo;

  @Column(name = "descricao", nullable = false)
  private String descricao;

  @Column(name = "valor_unitario", nullable = false)
  private BigDecimal valorUnitario;

  @Column(name = "quantidade", nullable = false)
  private Integer quantidade;

  @Column(name = "referencia_id", nullable = false)
  private UUID referenciaId;
}
