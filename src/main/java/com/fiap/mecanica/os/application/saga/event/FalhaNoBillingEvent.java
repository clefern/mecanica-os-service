package com.fiap.mecanica.os.application.saga.event;

import java.util.UUID;

public record FalhaNoBillingEvent(
    UUID sagaId,
    UUID osId,
    String motivo) {
}
