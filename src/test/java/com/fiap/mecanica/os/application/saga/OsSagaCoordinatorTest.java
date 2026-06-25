package com.fiap.mecanica.os.application.saga;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fiap.mecanica.os.application.port.out.OrdemServicoRepositoryPort;
import com.fiap.mecanica.os.application.saga.command.GerarOrcamentoCommand;
import com.fiap.mecanica.os.application.saga.command.IniciarExecucaoCommand;
import com.fiap.mecanica.os.application.saga.command.ReservarPecasCommand;
import com.fiap.mecanica.os.application.saga.event.ExecucaoFinalizadaEvent;
import com.fiap.mecanica.os.application.saga.event.FalhaNaExecucaoEvent;
import com.fiap.mecanica.os.application.saga.event.FalhaNoBillingEvent;
import com.fiap.mecanica.os.application.saga.event.FalhaNaReservaEvent;
import com.fiap.mecanica.os.application.saga.event.OrcamentoCriadoEvent;
import com.fiap.mecanica.os.application.saga.event.PagamentoConfirmadoEvent;
import com.fiap.mecanica.os.application.saga.event.PagamentoRecusadoEvent;
import com.fiap.mecanica.os.application.saga.event.PecasReservadasEvent;
import com.fiap.mecanica.os.domain.enums.StatusOS;
import com.fiap.mecanica.os.domain.enums.TipoItem;
import com.fiap.mecanica.os.domain.model.ItemOrdemServico;
import com.fiap.mecanica.os.domain.model.OrdemServico;
import com.fiap.mecanica.os.infra.persistence.entity.SagaStateEntity;
import com.fiap.mecanica.os.infra.persistence.repository.SagaStateJpaRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OsSagaCoordinatorTest {

  @Mock SagaStateJpaRepository sagaStateRepository;
  @Mock OrdemServicoRepositoryPort osRepository;
  @Mock OsSagaCoordinator.ReservarPecasCommandPublisher reservarPecasPublisher;
  @Mock OsSagaCoordinator.GerarOrcamentoCommandPublisher gerarOrcamentoPublisher;
  @Mock OsSagaCoordinator.IniciarExecucaoCommandPublisher iniciarExecucaoPublisher;

  @InjectMocks OsSagaCoordinator coordinator;

  private UUID osId;
  private UUID sagaId;
  private OrdemServico os;
  private SagaStateEntity sagaState;

  @BeforeEach
  void setUp() {
    osId = UUID.randomUUID();
    sagaId = UUID.randomUUID();

    os = OrdemServico.builder()
        .id(osId)
        .clienteId(UUID.randomUUID())
        .veiculoId(UUID.randomUUID())
        .mecanicoExecucaoId(UUID.randomUUID())
        .codigo("OS-TEST")
        .status(StatusOS.APROVADA)
        .valorTotal(BigDecimal.TEN)
        .itens(new ArrayList<>(List.of(
            ItemOrdemServico.builder()
                .id(UUID.randomUUID())
                .referenciaId(UUID.randomUUID())
                .tipo(TipoItem.PECA)
                .descricao("filtro")
                .valorUnitario(BigDecimal.TEN)
                .quantidade(1)
                .build())))
        .build();

    sagaState = SagaStateEntity.builder()
        .id(sagaId)
        .osId(osId)
        .status("INICIADA")
        .build();

    when(sagaStateRepository.save(any())).thenAnswer(i -> i.getArgument(0));
    when(osRepository.salvar(any())).thenAnswer(i -> i.getArgument(0));
  }

  // ── iniciarGeracaoOrcamento ───────────────────────────────────────────────

  @Test
  void iniciarGeracaoOrcamento_deveCriarSagaEPublicarCommand() {
    coordinator.iniciarGeracaoOrcamento(os, gerarOrcamentoPublisher);

    ArgumentCaptor<SagaStateEntity> sagaCaptor = ArgumentCaptor.forClass(SagaStateEntity.class);
    verify(sagaStateRepository).save(sagaCaptor.capture());
    assertThat(sagaCaptor.getValue().getStatus()).isEqualTo("INICIADA");

    ArgumentCaptor<GerarOrcamentoCommand> cmdCaptor = ArgumentCaptor.forClass(GerarOrcamentoCommand.class);
    verify(gerarOrcamentoPublisher).publicar(cmdCaptor.capture());
    assertThat(cmdCaptor.getValue().osId()).isEqualTo(osId);
    assertThat(cmdCaptor.getValue().itens()).hasSize(1);
  }

  // ── onOrcamentoCriado ─────────────────────────────────────────────────────

  @Test
  void onOrcamentoCriado_deveAtualizarSagaComOrcamentoId() {
    UUID orcamentoId = UUID.randomUUID();
    when(sagaStateRepository.findById(sagaId)).thenReturn(Optional.of(sagaState));

    coordinator.onOrcamentoCriado(new OrcamentoCriadoEvent(sagaId, osId, orcamentoId, "http://mp/pay"));

    verify(sagaStateRepository).save(any());
    assertThat(sagaState.getStatus()).isEqualTo("AGUARDANDO_PAGAMENTO");
    assertThat(sagaState.getOrcamentoId()).isEqualTo(orcamentoId);
    assertThat(sagaState.getPaymentUrl()).isEqualTo("http://mp/pay");
  }

  // ── onPagamentoConfirmado ─────────────────────────────────────────────────

  @Test
  void onPagamentoConfirmado_deveAprovarOsEPublicarReservarPecas() {
    os.setStatus(StatusOS.AGUARDANDO_APROVACAO);
    when(sagaStateRepository.findById(sagaId)).thenReturn(Optional.of(sagaState));
    when(osRepository.buscarPorId(osId)).thenReturn(Optional.of(os));

    coordinator.onPagamentoConfirmado(
        new PagamentoConfirmadoEvent(sagaId, osId, UUID.randomUUID(), "mp-123"),
        reservarPecasPublisher);

    assertThat(os.getStatus()).isEqualTo(StatusOS.APROVADA);
    verify(reservarPecasPublisher).publicar(argThat(cmd -> cmd.osId().equals(osId)));
    assertThat(sagaState.getStatus()).isEqualTo("AGUARDANDO_INVENTARIO");
  }

  // ── onPagamentoRecusado ───────────────────────────────────────────────────

  @Test
  void onPagamentoRecusado_deveCancelarOsECompensarSaga() {
    os.setStatus(StatusOS.AGUARDANDO_APROVACAO);
    when(sagaStateRepository.findById(sagaId)).thenReturn(Optional.of(sagaState));
    when(osRepository.buscarPorId(osId)).thenReturn(Optional.of(os));

    coordinator.onPagamentoRecusado(new PagamentoRecusadoEvent(sagaId, osId, UUID.randomUUID(), "saldo insuficiente"));

    assertThat(os.getStatus()).isEqualTo(StatusOS.CANCELADA);
    verify(sagaStateRepository, times(2)).save(any(SagaStateEntity.class));
    assertThat(sagaState.getStatus()).isEqualTo("COMPENSADA_BILLING");
  }

  // ── onFalhaNoBilling ──────────────────────────────────────────────────────

  @Test
  void onFalhaNoBilling_deveCancelarOsECompensarSaga() {
    os.setStatus(StatusOS.RECEBIDA);
    when(sagaStateRepository.findById(sagaId)).thenReturn(Optional.of(sagaState));
    when(osRepository.buscarPorId(osId)).thenReturn(Optional.of(os));

    coordinator.onFalhaNoBilling(new FalhaNoBillingEvent(sagaId, osId, "erro externo"));

    assertThat(os.getStatus()).isEqualTo(StatusOS.CANCELADA);
    verify(sagaStateRepository, times(2)).save(any(SagaStateEntity.class));
    assertThat(sagaState.getStatus()).isEqualTo("COMPENSADA_BILLING");
  }

  // ── onPecasReservadas ─────────────────────────────────────────────────────

  @Test
  void onPecasReservadas_deveIniciarExecucaoEPublicarCommandParaWorkshop() {
    os.setStatus(StatusOS.APROVADA);
    when(sagaStateRepository.findById(sagaId)).thenReturn(Optional.of(sagaState));
    when(osRepository.buscarPorId(osId)).thenReturn(Optional.of(os));

    coordinator.onPecasReservadas(new PecasReservadasEvent(sagaId, osId), iniciarExecucaoPublisher);

    assertThat(os.getStatus()).isEqualTo(StatusOS.EM_EXECUCAO);
    assertThat(sagaState.getStatus()).isEqualTo("AGUARDANDO_WORKSHOP");
    verify(iniciarExecucaoPublisher).publicar(argThat(cmd ->
        cmd.sagaId().equals(sagaId) && cmd.osId().equals(osId)));
  }

  // ── onFalhaNaReserva ──────────────────────────────────────────────────────

  @Test
  void onFalhaNaReserva_deveCancelarOsECompensarSaga() {
    os.setStatus(StatusOS.RECEBIDA);
    when(sagaStateRepository.findById(sagaId)).thenReturn(Optional.of(sagaState));
    when(osRepository.buscarPorId(osId)).thenReturn(Optional.of(os));

    coordinator.onFalhaNaReserva(new FalhaNaReservaEvent(sagaId, osId, "sem estoque"));

    assertThat(os.getStatus()).isEqualTo(StatusOS.CANCELADA);
    verify(sagaStateRepository, times(2)).save(any(SagaStateEntity.class));
    assertThat(sagaState.getStatus()).isEqualTo("COMPENSADA");
  }

  // ── onExecucaoFinalizada ──────────────────────────────────────────────────

  @Test
  void onExecucaoFinalizada_deveFinalizarOsEConcluirSaga() {
    os.setStatus(StatusOS.EM_EXECUCAO);
    UUID execucaoId = UUID.randomUUID();
    when(sagaStateRepository.findById(sagaId)).thenReturn(Optional.of(sagaState));
    when(osRepository.buscarPorId(osId)).thenReturn(Optional.of(os));

    coordinator.onExecucaoFinalizada(new ExecucaoFinalizadaEvent(sagaId, osId, execucaoId));

    assertThat(os.getStatus()).isEqualTo(StatusOS.FINALIZADA);
    assertThat(sagaState.getStatus()).isEqualTo("CONCLUIDA");
    assertThat(sagaState.getExecucaoId()).isEqualTo(execucaoId);
  }

  // ── onFalhaNaExecucao ─────────────────────────────────────────────────────

  @Test
  void onFalhaNaExecucao_deveCancelarOsECompensarSaga() {
    os.setStatus(StatusOS.EM_EXECUCAO);
    when(sagaStateRepository.findById(sagaId)).thenReturn(Optional.of(sagaState));
    when(osRepository.buscarPorId(osId)).thenReturn(Optional.of(os));

    coordinator.onFalhaNaExecucao(new FalhaNaExecucaoEvent(sagaId, osId, "mecânico indisponível"));

    assertThat(os.getStatus()).isEqualTo(StatusOS.CANCELADA);
    verify(sagaStateRepository, times(2)).save(any(SagaStateEntity.class));
    assertThat(sagaState.getStatus()).isEqualTo("COMPENSADA_WORKSHOP");
  }
}
