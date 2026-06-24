package com.fiap.mecanica.os.application.saga.event;

import java.util.UUID;

public record FalhaNaReservaEvent(UUID sagaId, UUID osId, String motivo) {}
