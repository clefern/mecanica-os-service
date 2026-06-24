package com.fiap.mecanica.os.application.saga.command;

import java.util.List;
import java.util.UUID;

public record ReservarPecasCommand(
    UUID sagaId,
    UUID osId,
    List<ItemReserva> itens) {

  public record ItemReserva(UUID referenciaId, String tipo, int quantidade) {}
}
