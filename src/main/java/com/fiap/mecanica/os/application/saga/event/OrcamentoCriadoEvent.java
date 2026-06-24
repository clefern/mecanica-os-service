package com.fiap.mecanica.os.application.saga.event;

import java.util.UUID;

public record OrcamentoCriadoEvent(
    UUID sagaId,
    UUID osId,
    UUID orcamentoId,
    String paymentUrl) {
}
