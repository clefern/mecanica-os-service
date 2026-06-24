package com.fiap.mecanica.os.application.saga;

import com.fiap.mecanica.os.application.port.out.OrdemServicoRepositoryPort;
import com.fiap.mecanica.os.application.saga.command.ReservarPecasCommand;
import com.fiap.mecanica.os.application.saga.event.FalhaNaReservaEvent;
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

    ReservarPecasCommand command = new ReservarPecasCommand(sagaId, os.getId(), itens);
    publisher.publicar(command);

    log.info("[SAGA] Iniciada sagaId={} osId={} itens={}", sagaId, os.getId(), itens.size());
  }

  @Transactional
  public void onPecasReservadas(PecasReservadasEvent event) {
    log.info("[SAGA] Peças reservadas sagaId={} osId={}", event.sagaId(), event.osId());
    sagaStateRepository.findById(event.sagaId()).ifPresent(state -> {
      state.setStatus("CONCLUIDA");
      sagaStateRepository.save(state);
    });
  }

  @Transactional
  public void onFalhaNaReserva(FalhaNaReservaEvent event) {
    log.warn("[SAGA] Falha na reserva sagaId={} osId={} motivo={}",
        event.sagaId(), event.osId(), event.motivo());

    sagaStateRepository.findById(event.sagaId()).ifPresent(state -> {
      state.setStatus("COMPENSANDO");
      sagaStateRepository.save(state);
    });

    osRepository.buscarPorId(event.osId()).ifPresent(os -> {
      os.cancelar();
      osRepository.salvar(os);
      log.info("[SAGA] Compensação: OS {} cancelada", event.osId());
    });

    sagaStateRepository.findById(event.sagaId()).ifPresent(state -> {
      state.setStatus("COMPENSADA");
      sagaStateRepository.save(state);
    });
  }

  public interface ReservarPecasCommandPublisher {
    void publicar(ReservarPecasCommand command);
  }
}
