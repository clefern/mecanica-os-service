package com.fiap.mecanica.os.application.saga;

import com.fiap.mecanica.os.application.port.out.OrdemServicoRepositoryPort;
import com.fiap.mecanica.os.application.saga.command.GerarOrcamentoCommand;
import com.fiap.mecanica.os.application.saga.command.ReservarPecasCommand;
import com.fiap.mecanica.os.application.saga.event.FalhaNoBillingEvent;
import com.fiap.mecanica.os.application.saga.event.FalhaNaReservaEvent;
import com.fiap.mecanica.os.application.saga.event.OrcamentoCriadoEvent;
import com.fiap.mecanica.os.application.saga.event.PagamentoConfirmadoEvent;
import com.fiap.mecanica.os.application.saga.event.PagamentoRecusadoEvent;
import com.fiap.mecanica.os.application.saga.event.PecasReservadasEvent;
import com.fiap.mecanica.os.domain.enums.TipoItem;
import com.fiap.mecanica.os.domain.model.OrdemServico;
import com.fiap.mecanica.os.infra.persistence.entity.SagaStateEntity;
import com.fiap.mecanica.os.infra.persistence.repository.SagaStateJpaRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OsSagaCoordinator {

  private final SagaStateJpaRepository sagaStateRepository;
  private final OrdemServicoRepositoryPort osRepository;

  // ── M1: bypass direto (PUT /aprovar) ──────────────────────────────────────

  public void iniciarReservaPecas(OrdemServico os, ReservarPecasCommandPublisher publisher) {
    UUID sagaId = UUID.randomUUID();

    SagaStateEntity state = SagaStateEntity.builder()
        .id(sagaId)
        .osId(os.getId())
        .status("INICIADA")
        .build();
    sagaStateRepository.save(state);

    List<ReservarPecasCommand.ItemReserva> itens = os.itensDoPTipo(TipoItem.PECA).stream()
        .map(i -> new ReservarPecasCommand.ItemReserva(
            i.getReferenciaId(), i.getTipo().name(), i.getQuantidade()))
        .toList();

    publisher.publicar(new ReservarPecasCommand(sagaId, os.getId(), itens));
    log.info("[SAGA] Reserva direta iniciada sagaId={} osId={}", sagaId, os.getId());
  }

  // ── M2: fluxo via billing ─────────────────────────────────────────────────

  public void iniciarGeracaoOrcamento(OrdemServico os, GerarOrcamentoCommandPublisher publisher) {
    UUID sagaId = UUID.randomUUID();

    SagaStateEntity state = SagaStateEntity.builder()
        .id(sagaId)
        .osId(os.getId())
        .status("INICIADA")
        .build();
    sagaStateRepository.save(state);

    List<GerarOrcamentoCommand.ItemOrcamento> itens = os.getItens().stream()
        .map(i -> new GerarOrcamentoCommand.ItemOrcamento(
            i.getReferenciaId(), i.getTipo().name(), i.getDescricao(),
            i.getValorUnitario(), i.getQuantidade()))
        .toList();

    publisher.publicar(new GerarOrcamentoCommand(sagaId, os.getId(), itens));
    log.info("[SAGA] GerarOrcamento iniciado sagaId={} osId={} itens={}", sagaId, os.getId(), itens.size());
  }

  @Transactional
  public void onOrcamentoCriado(OrcamentoCriadoEvent event) {
    log.info("[SAGA] OrcamentoCriado sagaId={} orcamentoId={}", event.sagaId(), event.orcamentoId());
    sagaStateRepository.findById(event.sagaId()).ifPresent(state -> {
      state.setStatus("AGUARDANDO_PAGAMENTO");
      state.setOrcamentoId(event.orcamentoId());
      state.setPaymentUrl(event.paymentUrl());
      sagaStateRepository.save(state);
    });
  }

  @Transactional
  public void onFalhaNoBilling(FalhaNoBillingEvent event) {
    log.warn("[SAGA] FalhaNoBilling sagaId={} motivo={}", event.sagaId(), event.motivo());
    sagaStateRepository.findById(event.sagaId()).ifPresent(state -> {
      state.setStatus("COMPENSANDO");
      sagaStateRepository.save(state);
    });
    osRepository.buscarPorId(event.osId()).ifPresent(os -> {
      os.cancelar();
      osRepository.salvar(os);
    });
    sagaStateRepository.findById(event.sagaId()).ifPresent(state -> {
      state.setStatus("COMPENSADA_BILLING");
      sagaStateRepository.save(state);
    });
  }

  @Transactional
  public void onPagamentoConfirmado(PagamentoConfirmadoEvent event,
      ReservarPecasCommandPublisher publisher) {
    log.info("[SAGA] PagamentoConfirmado sagaId={}", event.sagaId());

    sagaStateRepository.findById(event.sagaId()).ifPresent(state -> {
      state.setStatus("AGUARDANDO_INVENTARIO");
      sagaStateRepository.save(state);
    });

    osRepository.buscarPorId(event.osId()).ifPresent(os -> {
      os.aprovar();
      osRepository.salvar(os);

      List<ReservarPecasCommand.ItemReserva> itens = os.itensDoPTipo(TipoItem.PECA).stream()
          .map(i -> new ReservarPecasCommand.ItemReserva(
              i.getReferenciaId(), i.getTipo().name(), i.getQuantidade()))
          .toList();

      publisher.publicar(new ReservarPecasCommand(event.sagaId(), os.getId(), itens));
      log.info("[SAGA] ReservarPecas publicado após pagamento sagaId={}", event.sagaId());
    });
  }

  @Transactional
  public void onPagamentoRecusado(PagamentoRecusadoEvent event) {
    log.warn("[SAGA] PagamentoRecusado sagaId={} motivo={}", event.sagaId(), event.motivo());
    sagaStateRepository.findById(event.sagaId()).ifPresent(state -> {
      state.setStatus("COMPENSANDO");
      sagaStateRepository.save(state);
    });
    osRepository.buscarPorId(event.osId()).ifPresent(os -> {
      os.cancelar();
      osRepository.salvar(os);
    });
    sagaStateRepository.findById(event.sagaId()).ifPresent(state -> {
      state.setStatus("COMPENSADA_BILLING");
      sagaStateRepository.save(state);
    });
  }

  // ── Respostas do inventory (M1 reaproveitado) ─────────────────────────────

  @Transactional
  public void onPecasReservadas(PecasReservadasEvent event) {
    log.info("[SAGA] PecasReservadas sagaId={}", event.sagaId());
    sagaStateRepository.findById(event.sagaId()).ifPresent(state -> {
      state.setStatus("CONCLUIDA");
      sagaStateRepository.save(state);
    });
  }

  @Transactional
  public void onFalhaNaReserva(FalhaNaReservaEvent event) {
    log.warn("[SAGA] FalhaNaReserva sagaId={} motivo={}", event.sagaId(), event.motivo());
    sagaStateRepository.findById(event.sagaId()).ifPresent(state -> {
      state.setStatus("COMPENSANDO");
      sagaStateRepository.save(state);
    });
    osRepository.buscarPorId(event.osId()).ifPresent(os -> {
      os.cancelar();
      osRepository.salvar(os);
    });
    sagaStateRepository.findById(event.sagaId()).ifPresent(state -> {
      state.setStatus("COMPENSADA");
      sagaStateRepository.save(state);
    });
  }

  // ── Publisher interfaces ──────────────────────────────────────────────────

  public interface ReservarPecasCommandPublisher {
    void publicar(ReservarPecasCommand command);
  }

  public interface GerarOrcamentoCommandPublisher {
    void publicar(GerarOrcamentoCommand command);
  }
}
