package com.fiap.mecanica.os.application.service;

import com.fiap.mecanica.os.application.port.out.OrdemServicoRepositoryPort;
import com.fiap.mecanica.os.application.saga.OsSagaCoordinator;
import com.fiap.mecanica.os.domain.enums.TipoItem;
import com.fiap.mecanica.os.domain.exception.OrdemServicoNaoEncontradaException;
import com.fiap.mecanica.os.domain.model.ItemOrdemServico;
import com.fiap.mecanica.os.domain.model.OrdemServico;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fiap.mecanica.os.application.saga.OsSagaCoordinator.GerarOrcamentoCommandPublisher;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrdemServicoService {

  private final OrdemServicoRepositoryPort repository;
  private final OsSagaCoordinator sagaCoordinator;
  private final OsSagaCoordinator.ReservarPecasCommandPublisher commandPublisher;
  private final GerarOrcamentoCommandPublisher billingCommandPublisher;

  @Transactional
  public OrdemServico abrir(UUID clienteId, UUID veiculoId, UUID mecanicoId) {
    OrdemServico os = OrdemServico.nova(clienteId, veiculoId, mecanicoId);
    return repository.salvar(os);
  }

  @Transactional
  public OrdemServico adicionarItem(UUID osId, UUID referenciaId, TipoItem tipo,
      String descricao, BigDecimal valorUnitario, int quantidade) {
    OrdemServico os = buscarOuErrar(osId);
    ItemOrdemServico item = ItemOrdemServico.builder()
        .id(UUID.randomUUID())
        .referenciaId(referenciaId)
        .tipo(tipo)
        .descricao(descricao)
        .valorUnitario(valorUnitario)
        .quantidade(quantidade)
        .build();
    os.adicionarItem(item);
    return repository.salvar(os);
  }

  @Transactional
  public OrdemServico iniciarDiagnostico(UUID osId) {
    OrdemServico os = buscarOuErrar(osId);
    os.iniciarDiagnostico();
    return repository.salvar(os);
  }

  @Transactional
  public OrdemServico emitirOrcamento(UUID osId) {
    OrdemServico os = buscarOuErrar(osId);
    os.emitirOrcamento();
    repository.salvar(os);
    sagaCoordinator.iniciarGeracaoOrcamento(os, billingCommandPublisher);
    return os;
  }

  @Transactional
  public OrdemServico aprovar(UUID osId) {
    OrdemServico os = buscarOuErrar(osId);
    os.aprovar();
    repository.salvar(os);
    sagaCoordinator.iniciarReservaPecas(os, commandPublisher);
    return os;
  }

  @Transactional
  public OrdemServico cancelar(UUID osId) {
    OrdemServico os = buscarOuErrar(osId);
    os.cancelar();
    return repository.salvar(os);
  }

  @Transactional(readOnly = true)
  public OrdemServico buscarPorId(UUID id) {
    return buscarOuErrar(id);
  }

  @Transactional(readOnly = true)
  public Page<OrdemServico> listarTodas(Pageable pageable) {
    return repository.listarTodas(pageable);
  }

  private OrdemServico buscarOuErrar(UUID id) {
    return repository.buscarPorId(id)
        .orElseThrow(() -> new OrdemServicoNaoEncontradaException(id));
  }
}
