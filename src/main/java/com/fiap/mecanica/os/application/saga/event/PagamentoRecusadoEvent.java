package com.fiap.mecanica.os.application.saga.event;

import java.util.UUID;

public record PagamentoRecusadoEvent(
    UUID sagaId,
    UUID osId,
    UUID orcamentoId,
    String motivo) {
}
