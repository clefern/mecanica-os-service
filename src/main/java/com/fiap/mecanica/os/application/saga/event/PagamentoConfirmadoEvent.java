package com.fiap.mecanica.os.application.saga.event;

import java.util.UUID;

public record PagamentoConfirmadoEvent(
    UUID sagaId,
    UUID osId,
    UUID orcamentoId,
    String mpPaymentId) {
}
