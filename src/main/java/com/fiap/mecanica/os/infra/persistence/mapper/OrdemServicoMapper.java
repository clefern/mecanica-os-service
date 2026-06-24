package com.fiap.mecanica.os.infra.persistence.mapper;

import com.fiap.mecanica.os.domain.model.ItemOrdemServico;
import com.fiap.mecanica.os.domain.model.OrdemServico;
import com.fiap.mecanica.os.infra.persistence.entity.ItemOrdemServicoEntity;
import com.fiap.mecanica.os.infra.persistence.entity.OrdemServicoEntity;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class OrdemServicoMapper {

  public OrdemServicoEntity toEntity(OrdemServico os) {
    OrdemServicoEntity entity = OrdemServicoEntity.builder()
        .id(os.getId())
        .clienteId(os.getClienteId())
        .veiculoId(os.getVeiculoId())
        .mecanicoExecucaoId(os.getMecanicoExecucaoId())
        .mecanicoDiagnosticoId(os.getMecanicoDiagnosticoId())
        .codigo(os.getCodigo())
        .status(os.getStatus())
        .valorTotal(os.getValorTotal())
        .dataEntrada(os.getDataEntrada())
        .dataPrevisao(os.getDataPrevisao())
        .dataFechamento(os.getDataFechamento())
        .dataAprovacao(os.getDataAprovacao())
        .observacoes(os.getObservacoes())
        .prioridade(os.getPrioridade())
        .build();

    List<ItemOrdemServicoEntity> itensEntity = new ArrayList<>();
    if (os.getItens() != null) {
      for (ItemOrdemServico item : os.getItens()) {
        ItemOrdemServicoEntity ie = ItemOrdemServicoEntity.builder()
            .id(item.getId())
            .ordemServico(entity)
            .tipo(item.getTipo())
            .descricao(item.getDescricao())
            .valorUnitario(item.getValorUnitario())
            .quantidade(item.getQuantidade())
            .referenciaId(item.getReferenciaId())
            .build();
        itensEntity.add(ie);
      }
    }
    entity.setItens(itensEntity);
    return entity;
  }

  public OrdemServico toDomain(OrdemServicoEntity entity) {
    List<ItemOrdemServico> itens = new ArrayList<>();
    if (entity.getItens() != null) {
      for (ItemOrdemServicoEntity ie : entity.getItens()) {
        itens.add(ItemOrdemServico.builder()
            .id(ie.getId())
            .tipo(ie.getTipo())
            .descricao(ie.getDescricao())
            .valorUnitario(ie.getValorUnitario())
            .quantidade(ie.getQuantidade())
            .referenciaId(ie.getReferenciaId())
            .build());
      }
    }
    return OrdemServico.builder()
        .id(entity.getId())
        .clienteId(entity.getClienteId())
        .veiculoId(entity.getVeiculoId())
        .mecanicoExecucaoId(entity.getMecanicoExecucaoId())
        .mecanicoDiagnosticoId(entity.getMecanicoDiagnosticoId())
        .codigo(entity.getCodigo())
        .status(entity.getStatus())
        .valorTotal(entity.getValorTotal())
        .dataEntrada(entity.getDataEntrada())
        .dataPrevisao(entity.getDataPrevisao())
        .dataFechamento(entity.getDataFechamento())
        .dataAprovacao(entity.getDataAprovacao())
        .observacoes(entity.getObservacoes())
        .prioridade(entity.getPrioridade())
        .itens(itens)
        .build();
  }
}
